package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.configuration.WebMvcConfiguration;
import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(WebMvcConfiguration.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToUsersEndpoint_shouldReturnUsersView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.USER,
                accountId,
                true);

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(userService.getAllByAccountIdAndFilterOrdered(any(), any(), any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/users").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("user","users", "filter", "errorMessage"));
        verify(userService, times(1)).getById(any());
        verify(userService, times(1)).getAllByAccountIdAndFilterOrdered(any(), any(), any());
    }

    @Test
    void getAuthorizedRequestToNewUserEndpointValidUserLimit_shouldReturnUsersView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.PRIMARY_ADMIN,
                accountId,
                true);
        Plan mockPlan = new Plan();
        mockPlan.setPlanName(PlanName.PLUS);

        Account mockAccount = new Account();
        mockAccount.setPlan(mockPlan);

        when(accountService.getById(any())).thenReturn(mockAccount);
        doNothing().when(userService).validateUserLimit(any(), any());

        mockMvc.perform(get("/users/new-user").param("filter", "current").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("new-user"))
                .andExpect(model().attributeExists("userRequest","planNameString", "filter"));
        verify(accountService, times(1)).getById(any());
        verify(userService, times(1)).validateUserLimit(any(), any());
    }

    @Test
    void postAuthorizedRequestToNewUserEndpointWithCorrectData_redirectToUsers() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.PRIMARY_ADMIN,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.PRIMARY_ADMIN);

        MockHttpServletRequestBuilder request = post("/users")
                .with(user(data))
                .param("filter", "current")
                .formField("email", "ivan@mail.com")
                .formField("password", "1212")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        when(userService.getById(any())).thenReturn(mockUser);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
        verify(userService, times(1)).register(any(), any());
    }

    @Test
    void postAuthorizedRequestToNewUserEndpointWithIncorrectData_shouldReturnUsersView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.PRIMARY_ADMIN,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.PRIMARY_ADMIN);

        MockHttpServletRequestBuilder request = post("/users")
                .with(user(data))
                .param("filter", "current")
                .formField("email", "ivan@mail.com")
                .formField("password", "")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        when(userService.getById(any())).thenReturn(mockUser);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-user"));
        verify(userService, never()).register(any(), any());
    }

    @Test
    void postUnauthorizedRequestToNewUserEndpoint_throwsException() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.USER,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.USER);

        MockHttpServletRequestBuilder request = post("/users")
                .with(user(data))
                .param("filter", "current")
                .formField("email", "ivan@mail.com")
                .formField("password", "1212")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
        verify(userService, never()).register(any(), any());
    }

    @Test
    void getAuthorizedRequestToUserIdEndpoint_shouldReturnUserIdView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.PRIMARY_ADMIN,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.PRIMARY_ADMIN);

        when(userService.getById(any())).thenReturn(mockUser);

        mockMvc.perform(get("/users/{id}", UUID.randomUUID())
                        .param("filter", "current")
                        .with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("new-user"))
                .andExpect(model().attributeExists("editUserRequest","userId", "filter"));
        verify(userService, times(1)).getById(any());
    }

    @Test
    void getUnauthorizedRequestToUserIdEndpointUserRole_shouldReturnUserIdView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.USER,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.USER);

        when(userService.getById(any())).thenReturn(mockUser);

        mockMvc.perform(get("/users/{id}", UUID.randomUUID()))
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }

    @Test
    void getUnauthorizedRequestToUserIdEndpoint_shouldRedirectToUsers() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);
        User mockPrimaryAdmin = new User();
        mockPrimaryAdmin.setId(UUID.randomUUID());
        mockPrimaryAdmin.setAccountId(UUID.randomUUID());
        mockPrimaryAdmin.setUserRole(UserRole.PRIMARY_ADMIN);

        when(userService.getById(any())).thenReturn(mockPrimaryAdmin);

        mockMvc.perform(get("/users/{id}", UUID.randomUUID())
                        .param("filter", "current")
                        .with(user(data)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("filter", "errorMessage"));
        verify(userService, times(1)).getById(any());
    }

    @Test
    void putAuthorizedRequestToNewUserEndpoint_happyPath() throws Exception {

        doNothing().when(userService).edit(any(), any(), any());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.ADMINISTRATOR);

        MockHttpServletRequestBuilder request = put("/users/{id}", UUID.randomUUID())
                .with(user(data))
                .param("filter", "current")
                .formField("email", "ivan@mail.com")
                .formField("password", "")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("filter"));
        verify(userService, times(1)).edit(any(), any(), any());
    }

    @Test
    void putAuthorizedIncorrectDataRequestToNewUserEndpoint_shouldReturnUserIdView() throws Exception {

        doNothing().when(userService).edit(any(), any(), any());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setAccountId(accountId);
        mockUser.setUserRole(UserRole.ADMINISTRATOR);

        MockHttpServletRequestBuilder request = put("/users/{id}", UUID.randomUUID())
                .with(user(data))
                .param("filter", "current")
                .formField("email", "ivan@mail.com")
                .formField("password", "1212")
                .formField("firstName", "Ivan")
                .formField("lastName", "")
                .formField("userRoleString", "USER")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-user"))
                .andExpect(model().attributeExists("filter", "userId", "editUserRequest"));
        verify(userService, never()).edit(any(), any(), any());
    }

    @Test
    void putAuthorizedRequestToStatusEndpoint_happyPath() throws Exception {

        doNothing().when(userService).switchStatus(any(), any());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);

        mockMvc.perform(put("/users/{id}/status", UUID.randomUUID())
                        .param("filter", "current")
                        .with(user(data))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("filter"));
        verify(userService, times(1)).switchStatus(any(), any());
    }

    @Test
    void putAuthorizedRequestToArchiveStatusEndpoint_happyPath() throws Exception {

        doNothing().when(userService).switchArchveStatus(any(), any());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);

        mockMvc.perform(put("/users/{id}/archive-status", UUID.randomUUID())
                        .param("filter", "current")
                        .with(user(data))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("filter"));
        verify(userService, times(1)).switchArchveStatus(any(), any());
    }

    @Test
    void putAuthorizedRequestToDeleteEndpoint_happyPath() throws Exception {

        doNothing().when(userService).delete(any(), any());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.ADMINISTRATOR,
                accountId,
                true);

        mockMvc.perform(delete("/users/{id}", UUID.randomUUID())
                        .param("filter", "current")
                        .with(user(data))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attributeExists("filter"));
        verify(userService, times(1)).delete(any(), any());
    }
}
