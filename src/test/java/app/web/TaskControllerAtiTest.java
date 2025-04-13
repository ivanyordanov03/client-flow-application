package app.web;

import app.TestBuilder;
import app.configuration.WebMvcConfiguration;
import app.security.AuthenticationMetadata;
import app.task.model.Task;
import app.task.service.TaskService;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(WebMvcConfiguration.class)
public class TaskControllerAtiTest {

    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToTasksEndpoint_shouldReturnTasksView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();
        Task task = TestBuilder.aTask();

        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(taskService.getAllForUserRoleUserByAccountIdUserIdAndFilter(any(), any())).thenReturn(List.of(task));
        when(taskService.getAllByAccountIdUserIdAndFilter(any(), any(), any())).thenReturn(List.of(task));
        when(taskService.getAllByAccountId(any())).thenReturn(List.of(task));
        when(taskService.getMyTasks(any())).thenReturn(List.of(task));

        mockMvc.perform(get("/tasks").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"))
                .andExpect(model().attributeExists("user", "filter", "userFirstNameAndLastNameInitial", "allAccountTasks", "myTasks"));
        verify(userService, times(1)).getById(any());
        verify(taskService, times(1)).getAllForUserRoleUserByAccountIdUserIdAndFilter(any(), any());
        verify(taskService, never()).getAllByAccountIdUserIdAndFilter(any(), any(), any());
        verify(taskService, times(1)).getAllByAccountId(any());
        verify(taskService, times(1)).getMyTasks(any());
    }
}
