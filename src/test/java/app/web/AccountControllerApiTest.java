package app.web;

import app.TestBuilder;
import app.account.model.Account;
import app.account.service.AccountService;
import app.configuration.WebMvcConfiguration;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(WebMvcConfiguration.class)
public class AccountControllerApiTest {

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthorizedRequestToAccountEndpoint_shouldReturnAccountView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = TestBuilder.aAccount();
        account.setOwnerId(data.getUserId());

        when(userService.getById(any())).thenReturn(user);
        when(accountService.getById(any())).thenReturn(account);

        mockMvc.perform(get("/account").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(model().attributeExists("account", "user", "currentPlan"));
        verify(userService, times(1)).getById(any());
        verify(accountService, times(1)).getById(any());
    }

    @Test
    void getAuthorizedRequestToSettingsEndpoint_shouldReturnAccountSettingsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        Account account = TestBuilder.aAccount();
        account.setOwnerId(data.getUserId());

        when(accountService.getByOwnerId(any())).thenReturn(account);

        mockMvc.perform(get("/account/settings").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("account-settings"))
                .andExpect(model().attributeExists("accountRequest"));
        verify(accountService, times(1)).getByOwnerId(any());
    }

    @Test
    void postAuthorizedRequestToAccountSettingsEndpointWithCorrectData_redirectToAccountView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();

        MockHttpServletRequestBuilder request = post("/account")
                .with(user(data))
                .formField("logoURL", "")
                .formField("businessName", "Business Name")
                .formField("address", "Address, City, State")
                .formField("phoneNumber", "2023034455")
                .with(csrf());

        doNothing().when(accountService).edit(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
        verify(accountService, times(1)).edit(any(), any());
    }

    @Test
    void postAuthorizedRequestToAccountSettingsEndpointWithIncorrectData_returnToAccountSettingsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();

        MockHttpServletRequestBuilder request = post("/account")
                .with(user(data))
                .formField("logoURL", "some text")
                .formField("businessName", "Business Name")
                .formField("address", "Address, City, State")
                .formField("phoneNumber", "202303445")
                .with(csrf());

        doNothing().when(accountService).edit(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("account-settings"));
        verify(accountService, never()).edit(any(), any());
    }

    @Test
    void putAuthorizedRequestToSetAutoRenewalEndpoint_redirectToPaymentSettingsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();

        MockHttpServletRequestBuilder request = put("/account/{id}/auto-renewal", data.getUserId())
                .with(user(data))
                .with(csrf());
        doNothing().when(accountService).switchAutoRenewal(any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment-settings"));
        verify(accountService, times(1)).switchAutoRenewal(any());
    }
}
