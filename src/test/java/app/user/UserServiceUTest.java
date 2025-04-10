package app.user;

import app.TestBuilder;
import app.account.service.AccountService;
import app.plan.properties.PlanProperties;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PlanProperties planProperties;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistingUser_whenEditedTheirProfileWithEmptyPassword_thenKeepOldPasswordAndSaveToDatabase() {

        // Given
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
        // When
        userService.edit(user.getId(), dto, loggedUser.getId());

        // Then
        assertEquals("Ivan", user.getFirstName());
        assertEquals("Yo", user.getLastName());
        assertEquals("ivan@mail.com", user.getEmail());
        assertEquals(currentPassword, user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingPrimaryAdmin_whenEditingOwnProfileNewPasswordAndRoleOnly_thenSaveToDatabase() {

        // Given
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
        // When
        userService.edit(loggedUser.getId(), dto, loggedUser.getId());

        // Then
        assertEquals(currentRole, loggedUser.getUserRole().toString());
        assertNotEquals(currentPassword, loggedUser.getPassword());
        assertEquals("boyan@gmail.com", loggedUser.getEmail());
        verify(userRepository, times(1)).save(loggedUser);
    }

    @Test
    void givenExistingPrimaryAdmin_whenEditedByUnauthorizedUser_throwsIllegalStateException() {

        // Given
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
        // When
        assertThrows(IllegalStateException.class, () -> userService.edit(user.getId(), dto, loggedUser.getId()));
    }

    @Test
    void givenAccountWithMaxUsersReached_whenAddNewUser_throwsIllegalStateException() {
        // Given
        User user = TestBuilder.aRandomPrimaryAdmin();
        when(userService.getAllByAccountIdNotArchivedOrdered(any())).thenReturn(List.of(user));
        // When
        assertThrows(IllegalStateException.class, () -> userService.validateUserLimit(user.getId(), "SIMPLE_START"));
    }
    @Test
    void whenGivenPlanNamePlus_ReturnPlanPropertiesMaxUserPlus() {
        assertEquals(planProperties.getMaxUsersPlus(), userService.getPlanMaxUsersFromPlanNameString("PLUS"));
    }
}
