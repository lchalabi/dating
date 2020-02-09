package service;

import dao.DatingDao;
import model.RelationshipResponse;
import model.RelationshipStatus;
import model.User;
import model.UserResponse;
import model.UserRelationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.ValidationService.TransactionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatingService {

    private final DatingDao datingDao;
    private final ValidationService validationService;

    @Autowired
    public DatingService(DatingDao datingDao, ValidationService validationService) {
        this.datingDao = datingDao;
        this.validationService = validationService;
    }

    public List<User> getAll() {
        return datingDao.getAll();
    }

    public UserResponse updateUser(User updateUser) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validateUser(updateUser, TransactionType.UPDATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            datingDao.updateUser(updateUser);
        }
        return userResponse;
    }

    public UserResponse createUser(User newUser) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validateUser(newUser, TransactionType.CREATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            Optional<User> createdUser = datingDao.createUser(newUser);
            createdUser.ifPresent(user -> userResponse.setUsers(Collections.singletonList(user)));
        }
        return userResponse;
    }

    /**
     * Returns a list of users that the current user has either blocked or matched with, or that have liked the
     * current user.
     */
    public List<User> getRelationshipsByStatus(int userId, RelationshipStatus status) {
        if (status == RelationshipStatus.BLOCKED || status == RelationshipStatus.MATCHED) {
            return datingDao.getRelationshipsByStatus(userId, status);
        } else {
            return datingDao.getUsersWhoLiked(userId);
        }
    }

    /**
     * Returns a list of users.  Includes users that have not blocked the current user, that the current user has not
     * already liked, blocked, or matched with.  The list is sorted by whether a user in the list has liked the
     * current user and then by if their preference in ice cream is the same.  Ex: a user that has liked the
     * current user and has the same ice cream preference as the current user will appear before a user in the list
     * that has liked the current user but has a different ice cream preference.  Users that have not liked the
     * current user will appear at the bottom of the list.
     */
    public UserResponse getRecommendedUsers(int userId) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validateUserExists(userId))
            .build();
        final Optional<User> user = datingDao.getById(userId);
        if (userResponse.getFailures().isEmpty() && user.isPresent()) {
            List<User> users = datingDao.getViewableUsers(userId);
            final Set<Integer> likedBy =
                datingDao.getUsersWhoLiked(user.get().getId()).stream().map(User::getId).collect(Collectors.toSet());
            Comparator<User> comparator = Comparator.<User, Integer>comparing(user2 -> user2.likedBy(likedBy))
                .thenComparing(user2 -> user2.getSimilarity(user.get()));

            comparator.thenComparing(user1 -> user1.likedBy(likedBy));
            users.sort(comparator);
            userResponse.setUsers(users);
        }
        return userResponse;
    }

    /**
     * Upserts a change in relationship between two users.  Possible relationship statuses are BLOCKED, LIKED,
     * DISLIKED, and MATCHED. The user_relationships table is unidirectional, therefore if the upsert represents a
     * match, then both relationships will be changed to represent the match.  If theupsert represents a block, then
     * the counterparty relationship will be removed and the blocked user will not be able to interact with the
     * blocking user in any way.  Blocked users will not appear in recommendations or likes for the blocking user.
     * Blocking users will not appear in recommendations or likes for the blocked user.  Blocking users have the
     * ability to unblock blocked users by accessing them from the "/blocks" endpoint.
     * Upon unblock, both users will be able to interact with each other as if the block never happened.
     */
    public RelationshipResponse upsertRelationship(UserRelationship userRelationship) {
        RelationshipResponse relationshipResponse = RelationshipResponse.builder()
            .failures(validationService.validateUserRelationship(userRelationship))
            .build();

        Optional<UserRelationship> possibleMatch = checkForMatch(userRelationship);
        if (relationshipResponse.getFailures().isEmpty()) {
            if (userRelationship.getStatus() == RelationshipStatus.LIKED) {
                possibleMatch.ifPresent(match -> {
                    match.setStatus(RelationshipStatus.MATCHED);
                    userRelationship.setStatus(RelationshipStatus.MATCHED);
                    datingDao.upsertRelationship(match);
                    relationshipResponse.setMatch(true);
                });
            } else if (userRelationship.getStatus() == RelationshipStatus.BLOCKED) {
                possibleMatch.ifPresent(datingDao::deleteRelationship);
            } else if (userRelationship.getStatus() == RelationshipStatus.DISLIKED) {
                possibleMatch.ifPresent(match -> {
                    match.setStatus(RelationshipStatus.LIKED);
                    datingDao.upsertRelationship(match);
                });
            }
            datingDao.upsertRelationship(userRelationship);
        }
        return relationshipResponse;
    }

    private Optional<UserRelationship> checkForMatch(UserRelationship userRelationship) {
        Optional<UserRelationship> counterUserRelationship =
            datingDao.getUserRelationshipByIds(userRelationship.getUser2Id(), userRelationship.getUser1Id());
        if (counterUserRelationship.isPresent() &&
            (counterUserRelationship.get().getStatus() == RelationshipStatus.LIKED ||
                counterUserRelationship.get().getStatus() == RelationshipStatus.MATCHED)) {
            return counterUserRelationship;
        } else {
            return Optional.empty();
        }
    }
}
