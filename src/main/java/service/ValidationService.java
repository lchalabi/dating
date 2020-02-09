package service;

import dao.UserDao;
import model.User;
import model.UserRelationship;
import model.ValidationRule;
import model.ValidationFailure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static model.ValidationRule.EMAIL_ALREADY_EXISTS;
import static model.ValidationRule.EMAIL_MUST_BE_SPECIFIED;
import static model.ValidationRule.ID_MUST_BE_NULL;
import static model.ValidationRule.STATUS_MUST_BE_SPECIFIED;

@Component
public class ValidationService {

    private final UserDao userDao;

    public enum TransactionType {
        CREATE, UPDATE, DELETE;
    }

    @Autowired
    public ValidationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<ValidationFailure> validateUser(User user, TransactionType transactionType) {
        if (transactionType == TransactionType.CREATE) {
            return validateCreate(user);
        } else if (transactionType == TransactionType.UPDATE) {
            return validateUpdate(user);
        }
        return Collections.emptyList();
    }

    public List<ValidationFailure> validateUserRelationship(UserRelationship userRelationship) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        if (userRelationship.getStatus() == null) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(STATUS_MUST_BE_SPECIFIED.getDesc())
                .build());
        }
        validateUserExists(userRelationship.getUser1Id(), validationFailures);
        validateUserExists(userRelationship.getUser2Id(), validationFailures);
        return validationFailures;
    }

    private List<ValidationFailure> validateCreate(User user) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        if (user.getId() != null) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(ID_MUST_BE_NULL.getDesc())
                .build());
        }
        validateEmail(user, validationFailures);
        validateName(user, validationFailures);
        return validationFailures;
    }

    private List<ValidationFailure> validateUpdate(User user) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        if (user.getId() == null) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(ValidationRule.ID_MUST_NOT_BE_NULL.getDesc())
                .build());
        } else if (user.getId() != null){
            validateUserExists(user.getId(), validationFailures);
        }
        validateEmail(user, validationFailures);
        validateName(user, validationFailures);
        return validationFailures;
    }

    private void validateUserExists(int id, List<ValidationFailure> validationFailures) {
        Optional<User> userWithId = userDao.getById(id);
        if (userWithId.isEmpty()) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(ValidationRule.ID_MUST_BELONG_TO_A_USER.getDesc())
                .build());
        }
    }

    private void validateEmail(User user, List<ValidationFailure> failures) {
         if (user.getEmail() == null || user.getEmail().isEmpty()) {
            failures.add(ValidationFailure.builder()
                .errorMessage(EMAIL_MUST_BE_SPECIFIED.getDesc())
                .build());
        } else if (!user.getEmail().isEmpty()) {
            userDao.getByEmail(user.getEmail()).ifPresent(userWithSameEmail -> {
                if (!userWithSameEmail.getId().equals(user.getId())) {
                    failures.add(ValidationFailure.builder()
                        .errorMessage(EMAIL_ALREADY_EXISTS.getDesc())
                        .build());
                }
            });
        }
    }

    private void validateName(User user, List<ValidationFailure> failures) {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            failures.add(ValidationFailure.builder()
                .errorMessage(ValidationRule.FIRST_NAME_MUST_BE_SPECIFIED.getDesc())
                .build());
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            failures.add(ValidationFailure.builder()
                .errorMessage(ValidationRule.LAST_NAME_MUST_BE_SPECIFIED.getDesc())
                .build());
        }
    }

}
