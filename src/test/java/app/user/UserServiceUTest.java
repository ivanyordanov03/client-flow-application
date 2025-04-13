package app.user;

import app.TestBuilder;
import app.account.model.Account;
import app.account.service.AccountService;
import app.event.InAppNotificationEventPublisher;
import app.exception.EmailAlreadyInUseException;
import app.notification.service.NotificationService;
import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.plan.properties.PlanProperties;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditUserRequest;
import app.web.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    private static final String DEFAULT_FILTER = "current";
    private static final String ARCHIVED_FILTER = "archived";

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PlanProperties planProperties;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    NotificationService notificationService;
    @Mock
    InAppNotificationEventPublisher inAppNotificationEventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUser_whenEditedTheirProfileWithEmptyPassword_thenKeepOldPasswordAndSaveToDatabase() {

        EditUserRequest dto = EditUserRequest.builder()
                .firstName("Ivan")
                .lastName("Yo")
                .email("ivan@mail.com")
                .password("")
                .userRoleString("USER")
                .build();
        User user = TestBuilder.aRandomUser();
        String currentPassword = user.getPassword();
        User loggedUser = TestBuilder.aRandomPrimaryAdmin();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(loggedUser));

        userService.edit(user.getId(), dto, loggedUser.getId());

        assertEquals("Ivan", user.getFirstName());
        assertEquals("Yo", user.getLastName());
        assertEquals("ivan@mail.com", user.getEmail());
        assertEquals(currentPassword, user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingPrimaryAdmin_whenEditingOwnProfileNewPasswordAndRoleOnly_thenSaveToDatabase() {

        EditUserRequest dto = EditUserRequest.builder()
                .firstName("Boyan")
                .lastName("Kirov")
                .email("boyan@gmail.com")
                .password("newPassword")
                .userRoleString("USER")
                .build();

        User loggedUser = TestBuilder.aRandomPrimaryAdmin();
        String currentPassword = loggedUser.getPassword();
        String currentRole = loggedUser.getUserRole().toString();

        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(loggedUser));
//        doNothing().when(inAppNotificationEventPublisher).send(any());

        userService.edit(loggedUser.getId(), dto, loggedUser.getId());

        assertEquals(currentRole, loggedUser.getUserRole().toString());
        assertNotEquals(currentPassword, loggedUser.getPassword());
        assertEquals("boyan@gmail.com", loggedUser.getEmail());
        verify(userRepository, times(1)).save(loggedUser);
        verify(inAppNotificationEventPublisher, never()).send(any());
    }

    @Test
    void givenExistingPrimaryAdmin_whenEditedByUnauthorizedUser_throwsIllegalStateException() {


        EditUserRequest dto = EditUserRequest.builder()
                .firstName("Ivan")
                .lastName("Yo")
                .email("ivan@mail.com")
                .password("")
                .userRoleString("USER")
                .build();
        User user = TestBuilder.aRandomPrimaryAdmin();
        User loggedUser = TestBuilder.aRandomAdmin();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(loggedUser));

        assertThrows(IllegalStateException.class, () -> userService.edit(user.getId(), dto, loggedUser.getId()));
    }

    @Test
    void givenAccountWithMaxUsersReached_whenAddNewUser_throwsIllegalStateException() {

        User user = TestBuilder.aRandomPrimaryAdmin();
        when(userService.getAllByAccountIdNotArchivedOrdered(any())).thenReturn(List.of(user));

        assertThrows(IllegalStateException.class, () -> userService.validateUserLimit(user.getId(), "SIMPLE_START"));
    }
    @Test
    void whenGivenPlanNamePlus_ReturnPlanPropertiesMaxUserPlus() {
        assertEquals(planProperties.getMaxUsersPlus(), userService.getPlanMaxUsersFromPlanNameString("PLUS"));
    }

    @Test
    void givenInvalidPlanNameString_whenGettingPlanMaxUsers_throwsIllegalArgumentException() {

        String invalidPlanNameAsString = "invalid Plan Name";
        assertThrows(IllegalArgumentException.class, () -> userService.getPlanMaxUsersFromPlanNameString(invalidPlanNameAsString));
    }

    @Test
    void givenUserWithRolePrimaryAdmin_whenSwitchStatus_noAction() {

        User primaryAdmin = TestBuilder.aRandomPrimaryAdmin();
        when(userRepository.findById(primaryAdmin.getId())).thenReturn(Optional.of(primaryAdmin));

        userService.switchStatus(primaryAdmin.getId(), UUID.randomUUID());

        verify(userRepository, never()).save(primaryAdmin);
    }

    @Test
    void givenArchivedUser_whenSwitchStatus_noAction() {

        User user = TestBuilder.aRandomUser();
        user.setArchived(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.switchStatus(user.getId(), UUID.randomUUID());

        verify(userRepository, never()).save(user);
    }

    @Test
    void givenUserWithRolePrimaryAdmin_whenDelete_throwsIllegalStateException() {

        User primaryAdmin = TestBuilder.aRandomPrimaryAdmin();
        when(userRepository.findById(primaryAdmin.getId())).thenReturn(Optional.of(primaryAdmin));

        assertThrows(IllegalStateException.class, () -> userService.delete(primaryAdmin.getId(), UUID.randomUUID()));
    }

    @Test
    void givenEmailAddress_whenValidateAndExists_throwsEmailAlreadyInUseException() {

        User user = TestBuilder.aRandomUser();
        UserRequest userRequest = UserRequest.builder()
                .email(user.getEmail())
                .planName("ESSENTIALS")
                .build();
        Plan plan = Plan.builder()
                .planName(PlanName.ESSENTIALS)
                .build();
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .plan(plan)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findAllByAccountIdAndArchivedIsFalseOrderByUserRoleAscFirstNameAscLastNameAsc(account.getId())).thenReturn(List.of(user));
        when(planProperties.getMaxUsersEssentials()).thenReturn(5);

        assertThrows(EmailAlreadyInUseException.class, () -> userService.register(userRequest, account.getId()));
    }

    @Test
    void givenUserWithRoleUser_whenPullingAccountUsersList_thenReturnUsersListActiveOnly() {

        User user = TestBuilder.aRandomUser();
        List<User> userList = userService.getAllByAccountIdAndFilterOrdered(user.getAccountId(), "filter", user.getUserRole().name());

        assertNotNull(userList);
    }

    @Test
    void givenUserWithRoleAdministrator_whenPullingActiveAccountUsersList_thenReturnUsersListActiveOnly() {

        User admin = TestBuilder.aRandomAdmin();
        List<User> userList = userService.getAllByAccountIdAndFilterOrdered(admin.getAccountId(), DEFAULT_FILTER, admin.getUserRole().name());

        assertNotNull(userList);
    }

    @Test
    void givenUserWithRoleAdministrator_whenPullingArchivedAccountUsersList_thenReturnUsersListArchivedOnly() {

        User admin = TestBuilder.aRandomAdmin();
        List<User> userList = userService.getAllByAccountIdAndFilterOrdered(admin.getAccountId(), ARCHIVED_FILTER, admin.getUserRole().name());

        assertNotNull(userList);
    }

    @Test
    void givenUserWithRoleAdministrator_whenPullingAccountUsersListWithInvalidFilter_thenThrowsIllegalArgumentException() {

        User admin = TestBuilder.aRandomAdmin();
        String invalidFilterValue = "Invalid filter value";
        assertThrows(IllegalArgumentException.class, () -> userService.getAllByAccountIdAndFilterOrdered(admin.getAccountId(), invalidFilterValue, admin.getUserRole().name()));
    }
}
