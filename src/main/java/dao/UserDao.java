package dao;

import model.User;

import java.util.List;

public interface UserDao {

    List<User> getAll();
    List<User> getLikes(int userId);

}
