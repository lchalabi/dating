package dao;

import model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> getAll();
    void createUser(User newUser);
    void updateUser(User updateUser);
    List<User> getLikes(int userId);
    Optional<User> getByEmail(String email);
    Optional<User> getById(int id);

}
