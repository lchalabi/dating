package service;

import dao.DatingDao;
import model.IceCreamPreference;
import model.RelationshipStatus;
import model.User;
import model.UserRelationship;
import model.ValidationFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import service.ValidationService.TransactionType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ValidationService.class})
class ValidationTest {

    @MockBean
    DatingDao datingDao;

    @Autowired
    ValidationService validationService;

    private User user1 = User.builder()
        .id(1)
        .email("test@email.com")
        .iceCreamPreference(IceCreamPreference.AMPLE_HILLS)
        .firstName("test")
        .lastName("user")
        .build();

    private User user2 = User.builder()
        .id(2)
        .email("test2@email.com")
        .iceCreamPreference(IceCreamPreference.AMPLE_HILLS)
        .firstName("test2")
        .lastName("user2")
        .build();

    @BeforeEach
    void init() {
        mockDaoBehavior();
    }

    private void mockDaoBehavior() {
        Mockito.when(datingDao.getById(user1.getId())).thenReturn(Optional.of(user1));

        Mockito.when(datingDao.getById(user2.getId())).thenReturn(Optional.of(user2));

        Mockito.when(datingDao.getByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
        Mockito.when(datingDao.getByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
    }

    @Test
    void validateUserRelationship() {
        UserRelationship userRelationship = UserRelationship.builder()
            .user1Id(1)
            .user2Id(2)
            .status(RelationshipStatus.LIKED)
            .build();

        List<ValidationFailure> failures = validationService.validateUserRelationship(userRelationship);
        assertTrue(failures.isEmpty());

        UserRelationship userRelationshipWithBadUserId = UserRelationship.builder()
            .user1Id(1)
            .user2Id(3)
            .status(RelationshipStatus.LIKED)
            .build();

        List<ValidationFailure> failures2 = validationService.validateUserRelationship(userRelationshipWithBadUserId);
        assertFalse(failures2.isEmpty());

        UserRelationship userRelationshipWithoutStatus = UserRelationship.builder()
            .user1Id(1)
            .user2Id(3)
            .build();

        List<ValidationFailure> failures3 = validationService.validateUserRelationship(userRelationshipWithoutStatus);
        assertFalse(failures3.isEmpty());
    }

    @Test
    void validateUserCreate() {
        User userCreate = User.builder()
            .email("test3@email.com")
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures = validationService.validateUser(userCreate, TransactionType.CREATE);
        assertTrue(failures.isEmpty());

        User userCreateWithExistingEmail = User.builder()
            .email(user2.getEmail())
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures2 = validationService.validateUser(userCreateWithExistingEmail, TransactionType.CREATE);
        assertFalse(failures2.isEmpty());

        User userCreateWithId = User.builder()
            .id(1)
            .email("test4@email.com")
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures3 = validationService.validateUser(userCreateWithId,
            TransactionType.CREATE);
        assertFalse(failures3.isEmpty());
    }

    @Test
    void validateUserUpdate() {
        User userEdit = User.builder()
            .id(1)
            .email("test4@email.com")
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures = validationService.validateUser(userEdit, TransactionType.UPDATE);
        assertTrue(failures.isEmpty());

        User userUpdateWithExistingEmail = User.builder()
            .id(1)
            .email(user2.getEmail())
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures2 = validationService.validateUser(userUpdateWithExistingEmail,
            TransactionType.UPDATE);
        assertFalse(failures2.isEmpty());

        User userUpdateWithoutId = User.builder()
            .email(user2.getEmail())
            .firstName("test3")
            .lastName("user3")
            .build();

        List<ValidationFailure> failures3 = validationService.validateUser(userUpdateWithoutId,
            TransactionType.UPDATE);
        assertFalse(failures3.isEmpty());
    }
}
