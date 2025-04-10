package app;

import app.plan.service.PlanInit;
import app.task.model.Task;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.TaskRequest;
import app.web.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class MarkTaskAsCompleteITest {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PlanInit planInit;


    @Test
    void deleteMarkedCompleteTaskWithoutPriority_happyPath() {
        planInit.run();
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .password("password")
                .planName("ESSENTIALS")
                .userRoleString("USER")
                .build();

        userService.registerAccountOwner(userRequest);
        User primaryAdmin = userService.getByEmail("john@doe.com");
        UUID primaryAdminId = primaryAdmin.getId();

        TaskRequest taskRequest = TaskRequest.builder()
                .name("Task")
                .dueDate("2025-04-30")
                .assignedTo(primaryAdminId.toString())
                .build();
        taskService.createNew(taskRequest, primaryAdminId);
        Task task = taskService.getMyTasks(primaryAdminId).get(0);

        UUID taskId = task.getId();
        taskService.markAsComplete(taskId, primaryAdminId);

        assertTrue(taskService.getById(taskId).isCompleted());

        task = taskService.getMyTasks(primaryAdminId).get(0);
        taskService.delete(task.getId(), primaryAdminId);
        assertTrue(taskService.getMyTasks(primaryAdminId).isEmpty());
    }

    @Test
    void deleteMarkedCompleteTaskWithPriority_happyPath() {

        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .password("password")
                .planName("ESSENTIALS")
                .userRoleString("USER")
                .build();

        userService.registerAccountOwner(userRequest);
        User primaryAdmin = userService.getByEmail("john@doe.com");
        UUID primaryAdminId = primaryAdmin.getId();

        TaskRequest taskRequest = TaskRequest.builder()
                .name("Task")
                .dueDate("2025-04-30")
                .assignedTo(primaryAdminId.toString())
                .priority("HIGH")
                .build();
        taskService.createNew(taskRequest, primaryAdminId);
        Task task = taskService.getMyTasks(primaryAdminId).get(0);

        assertEquals("HIGH", task.getPriority().toString());

        UUID taskId = task.getId();
        taskService.markAsComplete(taskId, primaryAdminId);

        task = taskService.getById(taskId);
        assertTrue(task.isCompleted());
        assertTrue(task.getDateCompleted().isBefore(task.getDueDate()));
        taskService.delete(task.getId(), primaryAdminId);
        assertTrue(taskService.getMyTasks(primaryAdminId).isEmpty());
    }
}