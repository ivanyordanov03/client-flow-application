package app.task.service;

import app.event.InAppNotificationEventPublisher;
import app.event.payload.InAppNotificationEvent;
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

    private static final String USER = "USER";
    private static final String FIRST_NAME_LAST_NAME_INITIAL = "%s %s.";
    private static final String TASK_WITH_ID_NOT_FOUND = "Task with [%s] not found.";
    private static final String INVALID_TASK_FILTER_TYPE = "Invalid value [%s] for task filter.";
    private static final String NEW_TASK_WITH_ID_CREATED_BY_USER_WITH_ID = "New task with id [%s] was successfully created by user with id [%s].";
    private static final String TASK_WITH_ID_DELETED_BY_USER_WITH_ID = "Task with id [%s] was successfully deleted by user with id [%s].";
    private static final String TASK_WITH_ID_EDITED_BY_USER_WITH_ID = "Task with id [%s] was successfully edited by user with id [%s].";
    private static final String TASK_WITH_ID_MARKED_AS_COMPLETED = "Task with id [%s] marked completed by user with id [%s].";
    private static final String TASK_COMPLETED_TOPIC = "Task completed.";
    private static final String TASK_COMPLETED_BODY = "\"%s\" task you created was marked completed.";
    private static final String NEW_TASK_ASSIGNED_TOPIC = "New task Assigned.";
    private static final String NEW_TASK_ASSIGNED_BODY = "\"%s\" task has been assigned to you by %s %s.";

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final InAppNotificationEventPublisher inAppNotificationEventPublisher;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       UserService userService,
                       InAppNotificationEventPublisher inAppNotificationEventPublisher) {

        this.taskRepository = taskRepository;
        this.userService = userService;
        this.inAppNotificationEventPublisher = inAppNotificationEventPublisher;
    }

    public Task createNew(TaskRequest taskRequest, UUID userId) {

        Task task = initializeNew(taskRequest, userId);
        User user = userService.getById(userId);

        if (taskRequest.getPriority() != null && !taskRequest.getPriority().isBlank()) {
            task.setPriority(Mapper.mapTaskPriorityAsStringToTaskPriorityEnum(taskRequest.getPriority()));
        }

        taskRepository.save(task);

        if (!taskRequest.getAssignedTo().equals(userId.toString())) {
            InAppNotificationEvent event = new InAppNotificationEvent(task.getAssignedToId(),
                    NEW_TASK_ASSIGNED_TOPIC,
                    NEW_TASK_ASSIGNED_BODY.formatted(task.getName(), user.getFirstName(), user.getLastName()),
                    LocalDateTime.now());
            inAppNotificationEventPublisher.send(event);
        }
        log.info(NEW_TASK_WITH_ID_CREATED_BY_USER_WITH_ID.formatted(task.getId(), task.getCreatedById()));
        return task;
    }

    private Task initializeNew(TaskRequest taskRequest, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userService.getById(userId);
        User assignee = userService.getById(UUID.fromString(taskRequest.getAssignedTo()));
        return Task.builder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .dueDate(Mapper.mapDateAsStringIsoFormatToLocalDateFormat(taskRequest.getDueDate()))
                .accountId(user.getAccountId())
                .assignedToId(UUID.fromString(taskRequest.getAssignedTo()))
                .createdById(userId)
                .assignedToName(FIRST_NAME_LAST_NAME_INITIAL.formatted(assignee.getFirstName(), assignee.getLastName().charAt(0)))
                .createdByName(FIRST_NAME_LAST_NAME_INITIAL.formatted(user.getFirstName(), user.getLastName().charAt(0)))
                .dateCreated(now)
                .dateUpdated(now)
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
            case "completed" -> taskRepository.findAllByAssignedToIdAndCompletedIsTrueOrderByDateCompletedDesc(userId);
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
            case "completed" -> taskRepository.findAllByAccountIdAndCompletedIsTrueOrderByDateCompletedDesc(accountId);
            default -> throw new IllegalStateException(INVALID_TASK_FILTER_TYPE.formatted(filter));
        };
    }

    public List<Task> getAllDueToday(UUID accountId, UUID userId, String userRole) {

        if (USER.equals(userRole)) {
            return taskRepository.findAllByAssignedToIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(userId, LocalDate.now());
        } else {
            return taskRepository.findAllByAccountIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(accountId, LocalDate.now());
        }
    }

    public void markAsComplete(UUID id, UUID userId) {

        Task task = getById(id);
        task.setCompleted(true);
        task.setDateCompleted(LocalDate.now());
        task.setDateUpdated(LocalDateTime.now());

        taskRepository.save(task);

        if (!task.getAssignedToId().equals(task.getCreatedById())) {
            InAppNotificationEvent event = new InAppNotificationEvent(task.getCreatedById(),
                    TASK_COMPLETED_TOPIC,
                    TASK_COMPLETED_BODY.formatted(task.getName()),
                    LocalDateTime.now());
            inAppNotificationEventPublisher.send(event);
        }
        log.info(TASK_WITH_ID_MARKED_AS_COMPLETED.formatted(id, userId));
    }
    public Task getById(UUID id) {

        return taskRepository.findById(id).orElseThrow(() ->new IllegalArgumentException(TASK_WITH_ID_NOT_FOUND.formatted(id)));
    }

    public Task edit(UUID taskId, TaskRequest taskRequest, UUID userId) {

        Task task = getById(taskId);
        User user = userService.getById(userId);
        User assignee = userService.getById(UUID.fromString(taskRequest.getAssignedTo()));

        if (!taskRequest.getAssignedTo().equals(userId.toString()) && !task.getAssignedToId().equals(assignee.getId())) {
            InAppNotificationEvent event = new InAppNotificationEvent(task.getAssignedToId(),
                    NEW_TASK_ASSIGNED_TOPIC,
                    NEW_TASK_ASSIGNED_BODY.formatted(task.getName(), user.getFirstName(), user.getLastName()),
                    LocalDateTime.now());
            inAppNotificationEventPublisher.send(event);
        }

        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(Mapper.mapDateAsStringIsoFormatToLocalDateFormat(taskRequest.getDueDate()));
        task.setAssignedToId(assignee.getId());
        task.setAssignedToName(FIRST_NAME_LAST_NAME_INITIAL.formatted(assignee.getFirstName(), assignee.getLastName().charAt(0)));
        task.setCompleted(false);
        task.setPriority(taskRequest.getPriority().isBlank() ? null : Mapper.mapTaskPriorityAsStringToTaskPriorityEnum(taskRequest.getPriority()));
        task.setDateUpdated(LocalDateTime.now());

        taskRepository.save(task);
        log.info(TASK_WITH_ID_EDITED_BY_USER_WITH_ID.formatted(taskId, userId));

        return task;
    }

    public void delete(UUID id, UUID userId) {

        taskRepository.delete(getById(id));
        log.info(TASK_WITH_ID_DELETED_BY_USER_WITH_ID.formatted(id, userId));
    }

    public List<Task> getMyTasks(UUID userId) {

        return taskRepository.findAllByAssignedToId(userId);
    }
}