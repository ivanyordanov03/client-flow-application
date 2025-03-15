package app.task.service;

import app.task.model.Task;
import app.task.repository.TaskRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.TaskRequest;
import app.web.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {

    private static final String INVALID_TASK_FILTER_TYPE = "Invalid value [%s] for task filter.";
    private static final String USER_FIRST_NAME_LAST_NAME_INITIAL = "%s %s.";

    private final TaskRepository taskRepository;
    private final UserService userService;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserService userService) { // more tasks filters as well as action buttons

        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public void createNew(TaskRequest taskRequest, UUID userId) {

        Task task = initializeNew(taskRequest, userId);

        if (!taskRequest.getPriority().isBlank()) {
            task.setPriority(Mapper.getTaskPriorityFromString(taskRequest.getPriority()));
        }

        taskRepository.save(task);
    }

    private Task initializeNew(TaskRequest taskRequest, UUID userId) {

        LocalDateTime now = LocalDateTime.now();
        User user = userService.getById(userId);
        User assignee = userService.getById(UUID.fromString(taskRequest.getAssignedTo()));

        return Task.builder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .dueDate(Mapper.getDateFromStringIsoFormat(taskRequest.getDueDate()))
                .accountId(user.getAccountId())
                .assignedToId(UUID.fromString(taskRequest.getAssignedTo()))
                .createdById(userId)
                .assignedToName(USER_FIRST_NAME_LAST_NAME_INITIAL.formatted(assignee.getFirstName(), assignee.getLastName().charAt(0)))
                .createdByName(USER_FIRST_NAME_LAST_NAME_INITIAL.formatted(user.getFirstName(), user.getLastName().charAt(0)))
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public List<Task> getAllByAccountId(UUID accountId) {

        return taskRepository.findAllByAccountId(accountId);
    }

    public List<Task> getAllForUserRoleUserByAccountIdUserIdAndFilter(UUID userId, String filter) {

        return switch (filter) {
            case "my-tasks" -> taskRepository.findAllByAssignedToIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(userId);
            case "tasks-created" -> taskRepository.findAllByCreatedByIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(userId);
            case "due-today" -> taskRepository.findAllByAssignedToIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(userId, LocalDate.now());
            case "overdue" -> taskRepository.findAllByAssignedToIdAndDueDateBeforeAndCompletedIsFalseOrderByPriorityDescDueDateAsc(userId, LocalDate.now());
            case "upcoming" -> taskRepository.findAllByAssignedToIdAndDueDateAfterAndCompletedIsFalseOrderByDueDateAscPriorityDesc(userId, LocalDate.now());
            case "completed" -> taskRepository.findAllByAssignedToIdAndCompletedIsTrueOrderByCompletedOnDesc(userId);
            default -> throw new IllegalStateException(INVALID_TASK_FILTER_TYPE.formatted(filter));
        };
    }

    public List<Task> getAllByAccountIdUserIdAndFilter(UUID accountId, UUID userId, String filter) {

        return switch (filter) {
            case "all-open" -> taskRepository.findAllByAccountIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(accountId);
            case "my-tasks" -> taskRepository.findAllByAssignedToIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(userId);
            case "tasks-created" -> taskRepository.findAllByCreatedByIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(userId);
            case "due-today" -> taskRepository.findAllByAccountIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(accountId, LocalDate.now());
            case "overdue" -> taskRepository.findAllByAccountIdAndDueDateBeforeAndCompletedIsFalseOrderByPriorityDescDueDateAsc(accountId, LocalDate.now());
            case "upcoming" -> taskRepository.findAllByAccountIdAndDueDateAfterAndCompletedIsFalseOrderByDueDateAscPriorityDesc(accountId, LocalDate.now());
            case "completed" -> taskRepository.findAllByAccountIdAndCompletedIsTrueOrderByCompletedOnDesc(accountId);
            default -> throw new IllegalStateException(INVALID_TASK_FILTER_TYPE.formatted(filter));
        };
    }
}
