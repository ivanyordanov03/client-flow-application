package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.configuration.WebMvcConfiguration;
import app.contact.service.ContactService;
import app.exception.EmailAlreadyInUseException;
import app.notification.service.NotificationService;
import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.plan.service.PlanService;
import app.security.AuthenticationMetadata;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@Import(WebMvcConfiguration.class)
public class IndexControllerApiTest {

    @MockitoBean
    private PlanService planService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private ContactService contactService;
    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        mockMvc.perform(get("/login").param("error", "error"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void getRequestToRegisterEndpointWithParam_shouldReturnRegisterView() throws Exception {

        Plan mockPlan = new Plan();
        mockPlan.setPlanName(PlanName.PLUS);
        mockPlan.setPricePerMonth(BigDecimal.valueOf(29.99));

        when(planService.getByName(PlanName.PLUS)).thenReturn(mockPlan);

        mockMvc.perform(get("/register").param("plan", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("ownerRequest","currentPlan"));
    }

    @Test
    void getRequestToRegisterEndpointWithoutParam_shouldReturnRegisterView() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plans"));
    }

    @Test
    void getRequestToRegisterEndpointWithEmptyParam_shouldReturnRegisterView() throws Exception {

        mockMvc.perform(get("/register").param("plan", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plans"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        Plan mockPlan = new Plan();
        mockPlan.setPlanName(PlanName.PLUS);
        mockPlan.setPricePerMonth(BigDecimal.valueOf(29.99));

        when(planService.getByName(PlanName.PLUS)).thenReturn(mockPlan);

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "ivan@mail.com")
                .formField("password", "1212")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).registerAccountOwner(any());
    }

    @Test
    void postRequestToRegisterEndpointWithIncorrectData_shouldRedirectToRegister() throws Exception {

        Plan mockPlan = new Plan();
        mockPlan.setPlanName(PlanName.PLUS);
        mockPlan.setPricePerMonth(BigDecimal.valueOf(29.99));

        when(planService.getByName(PlanName.PLUS)).thenReturn(mockPlan);

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "ivan@mail.com")
                .formField("password", "12")
                .formField("firstName", "Ivan")
                .formField("lastName", "")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
        verify(userService, never()).registerAccountOwner(any());
    }

    @Test
    void postRequestToRegisterEndpointWhenEmailAlreadyInUse_shouldRedirectToRegisterWithFlashParameter() throws Exception {

        doThrow(new EmailAlreadyInUseException("Email already in use."))
                .when(userService)
                .registerAccountOwner(any(UserRequest.class));

        Plan mockPlan = new Plan();
        mockPlan.setPlanName(PlanName.PLUS);
        mockPlan.setPricePerMonth(BigDecimal.valueOf(29.99));

        when(planService.getByName(PlanName.PLUS)).thenReturn(mockPlan);

        MockHttpServletRequestBuilder request = post("/register")
                .formField("email", "ivan@mail.com")
                .formField("password", "123")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("userRoleString", "USER")
                .formField("planName", "PLUS")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/plans"))
                .andExpect(flash().attributeExists("emailInUseExceptionMessage", "filter"));
        verify(userService, times(1)).registerAccountOwner(any());
    }

    @Test
    void getAuthenticatedRequestToDashboardEndpointWhenAccountIsActive_shouldReturnDashboardView() throws Exception {

        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId,
                "user@mail.com",
                "1212",
                UserRole.USER,
                accountId,
                true);

        Account mockAccount = new Account();
        mockAccount.setActive(true);

        when(accountService.getById(any())).thenReturn(mockAccount);

        mockMvc.perform(get("/dashboard").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("user","contactsCount", "tasksDueTodayCount", "notificationsCounts"));
        verify(userService, times(1)).getById(any());
        verify(accountService, times(1)).getById(any());
    }

    @Test
    void getAuthenticatedRequestToDashboardEndpointWhenAccountIsInactiveAndUserRoleUser_shouldReturnLoginView() throws Exception {

        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId, "user@mail.com", "1212", UserRole.USER, accountId, true);

        Account mockAccount = new Account();
        mockAccount.setActive(false);

        when(accountService.getById(any())).thenReturn(mockAccount);

        mockMvc.perform(get("/dashboard").with(user(data)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).getById(any());
        verify(accountService, times(1)).getById(any());
    }

    @Test
    void getAuthenticatedRequestToDashboardEndpointWhenAccountIsInactiveAndUserRolePrimaryAdmin_shouldReturnNewPaymentView() throws Exception {

        User user = aRandomUser();
        user.setUserRole(UserRole.PRIMARY_ADMIN);
        when(userService.getById(any())).thenReturn(user);

        Account mockAccount = new Account();
        mockAccount.setActive(false);
        when(accountService.getById(any())).thenReturn(mockAccount);

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId, "user@mail.com", "1212", UserRole.PRIMARY_ADMIN, accountId, true);

        mockMvc.perform(get("/dashboard").with(user(data)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments/new"));
        verify(userService, times(1)).getById(any());
        verify(accountService, times(1)).getById(any());
    }

    @Test
    void getUnauthenticatedRequestToDashboardEndpoint_shouldRedirectLoginView() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        verify(userService, never()).getById(any());
        verify(accountService, never()).getById(any());
    }

    @Test
    void getAuthenticatedRequestToComingSoonEndpoint_shouldReturnComingSoonView() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AuthenticationMetadata data = new AuthenticationMetadata(userId, "user@mail.com", "1212", UserRole.USER, accountId, true);

        mockMvc.perform(get("/coming-soon").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("coming-soon"));
    }
}
