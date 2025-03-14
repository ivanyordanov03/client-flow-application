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
                .assignedToName(assignee.getFirstName() + " " + assignee.getLastName())
                .createdByName(user.getFirstName() + " " + user.getLastName())
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public List<Task> getAllByAccountId(UUID accountId) {

        return taskRepository.findAllByAccountId(accountId);
    }

    public List<Task> getAllByAccountIdAndAssignedToIdOrCreatedById(UUID accountId, UUID assignedToId, UUID createdById) {

        return taskRepository.findAllByAccountIdAndAssignedToIdOrCreatedById(accountId, assignedToId, createdById);
    }

    public List<Task> getAllForUserRoleByAccountUserAndFilter(UUID accountId, UUID userId, String filter) {

        return switch (filter) {
            case "may-tasks" -> taskRepository.findAllByAccountIdAndAssignedToIdOrCreatedById(accountId, userId, userId);
            case "due-today" -> taskRepository.findAllByAccountIdAndCompletedAndDueDateAndCreatedByIdOrAssignedToId(accountId, false, LocalDate.now(), userId, userId);
            case "overdue" -> taskRepository.findAllByAccountIdAndCompletedAndDueDateBeforeAndCreatedByIdOrAssignedToId(accountId, false, LocalDate.now(), userId, userId);
            case "upcoming" -> taskRepository.findAllByAccountIdAndCompletedAndDueDateAfterAndCreatedByIdOrAssignedToId(accountId, false, LocalDate.now(), userId, userId);
            case "completed" -> taskRepository.findAllByAccountIdAndCompletedAndCreatedByIdOrAssignedToId(accountId, true, userId, userId);
            default -> throw new IllegalStateException(INVALID_TASK_FILTER_TYPE.formatted(filter));
        };
    }

    public List<Task> getAllByAccountIdAndFilter(UUID accountId, UUID userId, String filter) {

        return switch (filter) {
            case "may-tasks" -> taskRepository.findAllByAccountIdAndAssignedToIdOrCreatedById(accountId, userId, userId);
            case "due-today" -> taskRepository.findAllByAccountIdAndCompletedAndDueDate(accountId, false, LocalDate.now());
            case "overdue" -> taskRepository.findAllByAccountIdAndCompletedAndDueDateBefore(accountId, false, LocalDate.now());
            case "upcoming" -> taskRepository.findAllByAccountIdAndCompletedAndDueDateAfter(accountId, false, LocalDate.now());
            case "completed" -> taskRepository.findAllByAccountIdAndCompleted(accountId, true);
            default -> throw new IllegalStateException(INVALID_TASK_FILTER_TYPE.formatted(filter));
        };
    }
}
