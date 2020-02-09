package service;

import dao.UserDao;
import model.RelationshipStatus;
import model.User;
import model.RestResponse;
import model.UserRelationship;
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

    public RestResponse updateUser(User updateUser) {
        RestResponse restResponse = RestResponse.builder()
            .failures(validationService.validateUser(updateUser, TransactionType.UPDATE))
            .build();
        if (restResponse.getFailures().isEmpty()) {
            userDao.updateUser(updateUser);
        }
        return restResponse;
    }

    public RestResponse createUser(User newUser) {
        RestResponse restResponse = RestResponse.builder()
            .failures(validationService.validateUser(newUser, TransactionType.CREATE))
            .build();
        if (restResponse.getFailures().isEmpty()) {
            userDao.createUser(newUser);
        }
        return restResponse;
    }

    public List<User> getRelationshipsByStatus(int userId, RelationshipStatus status) {
        return userDao.getRelationships(userId, status);
    }

    public RestResponse upsertRelationship(UserRelationship userRelationship) {
        RestResponse restResponse = RestResponse.builder()
            .failures(validationService.validateUserRelationship(userRelationship))
            .build();
        if (restResponse.getFailures().isEmpty()) {
            userDao.upsertRelationship(userRelationship);
        }
        return restResponse;
    }
}
