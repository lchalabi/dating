package dao;

import model.RelationshipStatus;
import model.User;
import model.UserRelationship;

import java.util.List;
import java.util.Optional;

public interface DatingDao {

    List<User> getAll();
    List<User> getViewableUsers(int userId);
    Optional<User> createUser(User newUser);
    void updateUser(User updateUser);
    List<User> getRelationshipsByStatus(int userId, RelationshipStatus status);
    List<User> getUsersWhoLiked(int userId);
    Optional<User> getByEmail(String email);
    void upsertRelationship(UserRelationship userRelationship);
    Optional<UserRelationship> getUserRelationshipByIds(int user1Id, int user2Id);
    void deleteRelationship(UserRelationship userRelationship);
    Optional<User> getById(int id);

}
