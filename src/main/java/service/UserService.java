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
            Optional<User> createdUser = userDao.createUser(newUser);
            createdUser.ifPresent(user -> restResponse.setUsers(Collections.singletonList(user)));
        }
        return restResponse;
    }

    public List<User> getRelationshipsByStatus(int userId, RelationshipStatus status) {
        if (status == RelationshipStatus.BLOCKED) {
            return userDao.getBlockedUsers(userId);
        } else {
            return userDao.getUsersWhoLiked(userId);
        }
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
