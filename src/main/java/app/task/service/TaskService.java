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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserService userService) {

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

        return Task.builder()
                .name(taskRequest.getName())
                .description(taskRequest.getDescription())
                .dueDate(Mapper.getDateFromStringIsoFormat(taskRequest.getDueDate()))
                .accountId(user.getAccountId())
                .assignedToId(UUID.fromString(taskRequest.getAssignedTo()))
                .createdById(userId)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public List<Task> getAllByAccountId(UUID accountId) {

        return taskRepository.findAllByAccountId(accountId);
    }

    public List<Task> getAllByAccountIdAndAssignedTo(UUID accountId, UUID assignedTo) {

        return taskRepository.findAllByAccountIdAndAssignedToUserId(accountId, assignedTo);
    }
}
