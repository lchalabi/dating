package service;

import dao.DatingDao;
import model.IceCreamPreference;
import model.RelationshipResponse;
import model.RelationshipStatus;
import model.User;
import model.UserRelationship;
import model.UserResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DatingService.class, ValidationService.class})
class DatingServiceTest {

    @MockBean
    DatingDao datingDao;

    @Autowired
    DatingService datingService;

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
        .firstName("test2")
        .lastName("user2")
        .iceCreamPreference(IceCreamPreference.VAN_LEEUWEN)
        .build();

    private User user3 = User.builder()
        .id(3)
        .email("test3@email.com")
        .iceCreamPreference(IceCreamPreference.AMPLE_HILLS)
        .firstName("test3")
        .lastName("user3")
        .build();

    private User user4 = User.builder()
        .id(4)
        .email("test4@email.com")
        .iceCreamPreference(IceCreamPreference.AMPLE_HILLS)
        .firstName("test4")
        .lastName("user4")
        .build();

    private User user5 = User.builder()
        .id(5)
        .email("test5@email.com")
        .firstName("test5")
        .lastName("user5")
        .iceCreamPreference(IceCreamPreference.AMPLE_HILLS)
        .build();

    @BeforeEach
    void init() {
        mockDaoBehavior();
    }

    private void mockDaoBehavior() {
        Mockito.when(datingDao.getById(user1.getId())).thenReturn(Optional.of(user1));

        Mockito.when(datingDao.getById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(datingDao.getById(user3.getId())).thenReturn(Optional.of(user3));
        Mockito.when(datingDao.getById(user4.getId())).thenReturn(Optional.of(user4));

        Mockito.when(datingDao.getByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
        Mockito.when(datingDao.getByEmail(user1.getEmail())).thenReturn(Optional.of(user1));

        Mockito.when(datingDao.getUserRelationshipByIds(user1.getId(), user3.getId()))
            .thenReturn(Optional.of(UserRelationship.builder()
                .status(RelationshipStatus.LIKED)
                .user1Id(user1.getId())
                .user2Id(user3.getId())
                .build()));

        Mockito.when(datingDao.getUserRelationshipByIds(user2.getId(), user3.getId()))
            .thenReturn(Optional.of(UserRelationship.builder()
                .status(RelationshipStatus.LIKED)
                .user1Id(user2.getId())
                .user2Id(user3.getId())
                .build()));

        Mockito.when(datingDao.getUserRelationshipByIds(user4.getId(), user3.getId()))
            .thenReturn(Optional.of(UserRelationship.builder()
                .status(RelationshipStatus.BLOCKED)
                .user1Id(user4.getId())
                .user2Id(user3.getId())
                .build()));

        Mockito.when(datingDao.getViewableUsers(user3.getId())).thenReturn(Arrays.asList(user2, user1, user5));
        Mockito.when(datingDao.getUsersWhoLiked(user3.getId())).thenReturn(Arrays.asList(user2, user1));
    }

    @Test
    void upsertRelationship() {
        UserRelationship userRelationship = UserRelationship.builder()
            .user1Id(user1.getId())
            .user2Id(user2.getId())
            .status(RelationshipStatus.LIKED)
            .build();

        RelationshipResponse relationshipResponse = datingService.upsertRelationship(userRelationship);
        assertFalse(relationshipResponse.isMatch());
        assertTrue(relationshipResponse.getFailures().isEmpty());

        UserRelationship userRelationshipMatch = UserRelationship.builder()
            .user1Id(user3.getId())
            .user2Id(user1.getId())
            .status(RelationshipStatus.LIKED)
            .build();

        RelationshipResponse relationshipResponse1 = datingService.upsertRelationship(userRelationshipMatch);
        assertTrue(relationshipResponse1.isMatch());
        assertTrue(relationshipResponse1.getFailures().isEmpty());
    }

    @Test
    void tryToLikeUserWhenBlocked() {
        UserRelationship userRelationship = UserRelationship.builder()
            .user1Id(user3.getId())
            .user2Id(user4.getId())
            .status(RelationshipStatus.LIKED)
            .build();

        RelationshipResponse responseWhenBlocked = datingService.upsertRelationship(userRelationship);
        assertFalse(responseWhenBlocked.getFailures().isEmpty());
    }

    @Test
    void getRecommendedUsers() {
        UserResponse userResponse = datingService.getRecommendedUsers(user3.getId());
        List<User> expectedOrder = Arrays.asList(user1, user2, user5);
        assertEquals(userResponse.getUsers(), expectedOrder);
    }

}
