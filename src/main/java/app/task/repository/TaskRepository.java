package app.task.repository;

import app.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByAccountId(UUID accountId);

    List<Task> findAllByAccountIdAndCompleted(UUID accountId, boolean b);

    List<Task> findAllByAccountIdAndCompletedAndDueDate(UUID accountId, boolean b, LocalDate now);

    List<Task> findAllByAccountIdAndCompletedAndDueDateBefore(UUID accountId, boolean b, LocalDate now);

    List<Task> findAllByAccountIdAndCompletedAndDueDateAfter(UUID accountId, boolean b, LocalDate now);

    List<Task> findAllByAccountIdAndAssignedToIdOrCreatedById(UUID accountId, UUID assignedToId, UUID createdById);

    List<Task> findAllByAccountIdAndCompletedAndCreatedByIdOrAssignedToId(UUID accountId, boolean completed, UUID createdById, UUID assignedToId);

    List<Task> findAllByAccountIdAndCompletedAndDueDateAfterAndCreatedByIdOrAssignedToId(UUID accountId, boolean completed, LocalDate now, UUID userId, UUID userId1);

    List<Task> findAllByAccountIdAndCompletedAndDueDateAndCreatedByIdOrAssignedToId(UUID accountId, boolean completed, LocalDate dueDate, UUID createdById, UUID assignedToId);

    List<Task> findAllByAccountIdAndCompletedAndDueDateBeforeAndCreatedByIdOrAssignedToId(UUID accountId, boolean completed, LocalDate dueDateBefore, UUID createdById, UUID assignedToId);
}