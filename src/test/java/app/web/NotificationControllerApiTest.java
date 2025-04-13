package app.web;

import app.TestBuilder;
import app.configuration.WebMvcConfiguration;
import app.notification.service.NotificationService;
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
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(WebMvcConfiguration.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToNotificationsEndpoint_shouldReturnNotificationView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();

        doNothing().when(notificationService).markAllAsRead(any());
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(notificationService.getNotifications(data.getUserId())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/notifications").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("user", "notifications"));
        verify(notificationService, times(1)).markAllAsRead(any());
        verify(userService, times(1)).getById(data.getUserId());
        verify(notificationService, times(1)).getNotifications(data.getUserId());
    }

    @Test
    void putAuthorizedRequestToNotificationsEndpoint_happyPath() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();

        doNothing().when(notificationService).archiveAll(any());
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(notificationService.getNotifications(data.getUserId())).thenReturn(new ArrayList<>());

        MockHttpServletRequestBuilder request = put("/notifications")
                .with(user(data))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }
}
