package dao;

import model.User;

import java.util.List;

public interface UserDao {

    List<User> getAll();
    void createUser(User newUser);
    List<User> getLikes(int userId);

}
