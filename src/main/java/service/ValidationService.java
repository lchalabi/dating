package service;

import dao.UserDao;
import model.RelationshipStatus;
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

import static model.ValidationRule.CANNOT_INTERACT_WITH_BLOCKED_USER;
import static model.ValidationRule.EMAIL_ALREADY_EXISTS;
import static model.ValidationRule.EMAIL_MUST_BE_SPECIFIED;
import static model.ValidationRule.ID_MUST_BE_NULL;
import static model.ValidationRule.STATUS_MUST_BE_SPECIFIED;
import static model.ValidationRule.USER_CANNOT_HAVE_A_RELATIONSHIP_WITH_SELF;

@Component
public class ValidationService {

    private final UserDao userDao;

    public enum TransactionType {
        CREATE, UPDATE
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
        if (userRelationship.getStatus() == null || userRelationship.getStatus() == RelationshipStatus.MATCHED) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(STATUS_MUST_BE_SPECIFIED.getDesc())
                .build());
        }
        if (userRelationship.getUser1Id() == userRelationship.getUser2Id()) {
            validationFailures.add(ValidationFailure.builder()
                .errorMessage(USER_CANNOT_HAVE_A_RELATIONSHIP_WITH_SELF.getDesc())
                .build());
        }
        validateUserExists(userRelationship.getUser1Id(), validationFailures);
        validateUserExists(userRelationship.getUser2Id(), validationFailures);

        if (validationFailures.isEmpty()) {
            if (isBlocked(userRelationship)) {
                validationFailures.add(ValidationFailure.builder()
                    .errorMessage(CANNOT_INTERACT_WITH_BLOCKED_USER.getDesc())
                    .build());
            }
        }
        return validationFailures;
    }

    public List<ValidationFailure> validateUserExists(int userId) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        validateUserExists(userId, validationFailures);
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

    private boolean isBlocked(UserRelationship userRelationship) {
        Optional<UserRelationship> counterUserRelationship =
            userDao.getUserRelationshipByIds(userRelationship.getUser2Id(), userRelationship.getUser1Id());
        return counterUserRelationship.filter(relationship -> relationship.getStatus() == RelationshipStatus.BLOCKED)
            .isPresent();
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
