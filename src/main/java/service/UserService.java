package service;

import dao.UserDao;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserDao userDao;
    private final ValidationService validationService;

    @Autowired
    public UserService(UserDao userDao, ValidationService validationService) {
        this.userDao = userDao;
        this.validationService = validationService;
    }

    public List<User> getAll() {
        return userDao.getAll();
    }

    public UserResponse updateUser(User updateUser) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validateUser(updateUser, TransactionType.UPDATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            userDao.updateUser(updateUser);
        }
        return userResponse;
    }

    public UserResponse createUser(User newUser) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validateUser(newUser, TransactionType.CREATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            Optional<User> createdUser = userDao.createUser(newUser);
            createdUser.ifPresent(user -> userResponse.setUsers(Collections.singletonList(user)));
        }
        return userResponse;
    }

    public List<User> getRelationshipsByStatus(int userId, RelationshipStatus status) {
        if (status == RelationshipStatus.BLOCKED || status == RelationshipStatus.MATCHED) {
            return userDao.getRelationshipsByStatus(userId, status);
        } else {
            return userDao.getUsersWhoLiked(userId);
        }
    }

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
                    userDao.upsertRelationship(match);
                    relationshipResponse.setMatch(true);
                });
            } else if (userRelationship.getStatus() == RelationshipStatus.BLOCKED) {
                possibleMatch.ifPresent(userDao::deleteRelationship);
            } else if (userRelationship.getStatus() == RelationshipStatus.DISLIKED) {
                possibleMatch.ifPresent(match -> {
                    match.setStatus(RelationshipStatus.LIKED);
                    userDao.upsertRelationship(match);
                });
            }
            userDao.upsertRelationship(userRelationship);
        }
        return relationshipResponse;
    }

    private Optional<UserRelationship> checkForMatch(UserRelationship userRelationship) {
        Optional<UserRelationship> counterUserRelationship =
            userDao.getUserRelationshipByIds(userRelationship.getUser2Id(), userRelationship.getUser1Id());
        if (counterUserRelationship.isPresent() &&
            (counterUserRelationship.get().getStatus() == RelationshipStatus.LIKED ||
                counterUserRelationship.get().getStatus() == RelationshipStatus.MATCHED)) {
            return counterUserRelationship;
        } else {
            return Optional.empty();
        }
    }
}
