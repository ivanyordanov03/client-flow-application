package app.task;

import app.TestBuilder;
import app.event.InAppNotificationEventPublisher;
import app.task.model.Task;
import app.task.model.TaskPriority;
import app.task.repository.TaskRepository;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.TaskRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceUTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InAppNotificationEventPublisher inAppNotificationEventPublisher;

    @InjectMocks
    private TaskService taskService;


    @Test
    void givenUserWithUserRoleUser_whenCallsGetAllDueTodayTaskList_happyPath() {
        User user = TestBuilder.aRandomUser();
        Task userTask = TestBuilder.aTask();
        userTask.setAssignedToId(user.getId());
        userTask.setAccountId(user.getAccountId());

        when(taskRepository.findAllByAssignedToIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(
                user.getId(), LocalDate.now()))
                .thenReturn(List.of(userTask));

        List<Task> allDueToday = taskService.getAllDueToday(
                user.getAccountId(),
                user.getId(),
                user.getUserRole().toString());

        assertEquals(1, allDueToday.size());
        assertEquals(userTask.getId(), allDueToday.get(0).getId());
    }

    @Test
    void givenUserWithNonUserRoleUser_whenCallsGetAllDueTodayTaskList_happyPath() {
        User user = TestBuilder.aRandomAdmin();
        Task userTask = TestBuilder.aTask();
        userTask.setAssignedToId(user.getId());
        userTask.setAccountId(user.getAccountId());

        when(taskRepository.findAllByAccountIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(
                user.getAccountId(), LocalDate.now()))
                .thenReturn(List.of(userTask));

        List<Task> allDueToday = taskService.getAllDueToday(
                user.getAccountId(),
                user.getId(),
                user.getUserRole().toString());

        assertEquals(1, allDueToday.size());
        assertEquals(userTask.getId(), allDueToday.get(0).getId());
    }

    @Test
    void givenTask_whenGivenCorrectDataAndEdited_happyPath() {

        User admin = TestBuilder.aRandomAdmin();
        User user = TestBuilder.aRandomUser();
        Task task = TestBuilder.aTask();
        task.setCompleted(true);
        task.setAssignedToId(admin.getId());

        TaskRequest taskRequest = TaskRequest.builder()
                .name("Edited title")
                .description("Edited description")
                .assignedTo(user.getId().toString())
                .priority(TaskPriority.MEDIUM.toString())
                .dueDate(LocalDate.now().plusDays(15).toString())
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getById(admin.getId())).thenReturn(admin);
        when(userService.getById(user.getId())).thenReturn(user);

        taskService.edit(task.getId(), taskRequest, admin.getId());

        assertEquals("Edited title", task.getName());
        assertEquals("Edited description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(LocalDate.now().plusDays(15), task.getDueDate());
        assertEquals(user.getId(), task.getAssignedToId());
        assertFalse(task.isCompleted());
        verify(taskRepository).save(task);
        verify(inAppNotificationEventPublisher).send(any());
    }

    @Test
    void givenTask_whenEditWithInvalidAssignee_throwsException() {

        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();

        Task existingTask = Task.builder().build();
        TaskRequest request = TaskRequest.builder()
                .assignedTo(invalidUserId.toString())
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(userService.getById(userId)).thenReturn(TestBuilder.aRandomUser());
        when(userService.getById(invalidUserId))
                .thenThrow(new IllegalArgumentException("User with id [%s] does not exist".formatted(invalidUserId)));

        assertThrows(IllegalArgumentException.class, () -> taskService.edit(taskId, request, userId));
    }

    @Test
    void requestUserTaskList_happyPath() {

        List<String> filter = List.of("my-tasks", "tasks-created", "due-today", "overdue", "upcoming", "completed");
        for (String f : filter) {
            assertNotNull(taskService.getAllForUserRoleUserByAccountIdUserIdAndFilter(UUID.randomUUID(), f));
        }
    }
    @Test
    void requestNonUserTaskList_happyPath() {

        List<String> filter = List.of("my-tasks", "tasks-created", "due-today", "overdue", "upcoming", "completed", "all-open");
        for (String f : filter) {
            assertNotNull(taskService.getAllByAccountIdUserIdAndFilter(UUID.randomUUID(), UUID.randomUUID(), f));
        }
    }

    @Test
    void taskLists_whenInvalidFilter_throwsIllegalStateException() {

        String invalidFilter = "invalid";

        assertThrows(IllegalStateException.class, () -> taskService.getAllByAccountIdUserIdAndFilter(UUID.randomUUID(), UUID.randomUUID(), invalidFilter));
        assertThrows(IllegalStateException.class, () -> taskService.getAllForUserRoleUserByAccountIdUserIdAndFilter(UUID.randomUUID(), invalidFilter));
    }

    @Test
    void givenAccount_whenCallGetMyTasks_happyPath () {
        User user = TestBuilder.aRandomAdmin();
        Task userTask = TestBuilder.aTask();
        userTask.setAssignedToId(user.getId());
        userTask.setAccountId(user.getAccountId());

        when(taskRepository.findAllByAssignedToId(user.getId())).thenReturn(List.of(userTask));

        List<Task> myTasks = taskService.getMyTasks(user.getId());
        assertEquals(1, myTasks.size());
        assertTrue(myTasks.contains(userTask));
    }

    @Test
    void delete_whenTaskNotFound_throwsException() {

        UUID nonExistentId = UUID.randomUUID();

        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> taskService.delete(nonExistentId, any()));
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void givenTask_whenEdit_happyPath() {
        User user = TestBuilder.aRandomAdmin();
        Task task = TestBuilder.aTask();

        TaskRequest taskRequest = TaskRequest.builder()
                .name("Edited title")
                .description("Edited description")
                .priority(TaskPriority.MEDIUM.toString())
                .dueDate(LocalDate.now().plusDays(15).toString())
                .assignedTo(user.getId().toString())
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getById(UUID.fromString(taskRequest.getAssignedTo()))).thenReturn(user);

        taskService.edit(task.getId(), taskRequest, user.getId());

        assertEquals("Edited title", task.getName());
        assertEquals("Edited description", task.getDescription());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(LocalDate.now().plusDays(15), task.getDueDate());
        assertEquals(user.getId(), task.getAssignedToId());
    }
}
