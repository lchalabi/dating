package service;

import dao.UserDao;
import model.User;
import model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.ValidationService.TransactionType;

import java.util.List;

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
            .failures(validationService.validate(updateUser, TransactionType.UPDATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            userDao.updateUser(updateUser);
        }
        return userResponse;
    }

    public UserResponse createUser(User newUser) {
        UserResponse userResponse = UserResponse.builder()
            .failures(validationService.validate(newUser, TransactionType.CREATE))
            .build();
        if (userResponse.getFailures().isEmpty()) {
            userDao.createUser(newUser);
        }
        return userResponse;
    }

    public List<User> getLikes(int userId) {
        return userDao.getLikes(userId);
    }
}
