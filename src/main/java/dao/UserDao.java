package dao;

import model.RelationshipStatus;
import model.User;
import model.UserRelationship;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> getAll();
    void createUser(User newUser);
    void updateUser(User updateUser);
    List<User> getRelationships(int userId, RelationshipStatus status);
    Optional<User> getByEmail(String email);
    void upsertRelationship(UserRelationship userRelationship);
    Optional<User> getById(int id);

}
