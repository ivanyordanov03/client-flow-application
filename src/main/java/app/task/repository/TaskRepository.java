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

    List<Task> findAllByAccountIdAndCompletedIsTrueOrderByDateCompletedDesc(UUID accountId);

    List<Task> findAllByAccountIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(UUID accountId);

    List<Task> findAllByAccountIdAndDueDateAfterAndCompletedIsFalseOrderByDueDateAscPriorityDesc(UUID accountId, LocalDate now);

    List<Task> findAllByAccountIdAndDueDateBeforeAndCompletedIsFalseOrderByPriorityDescDueDateAsc(UUID accountId, LocalDate now);

    List<Task> findAllByAccountIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(UUID accountId, LocalDate dueDate);

    List<Task> findAllByAssignedToIdAndCompletedIsTrueOrderByDateCompletedDesc(UUID assignedToId);

    List<Task> findAllByCreatedByIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(UUID createdById);

    List<Task> findAllByAssignedToIdAndDueDateAndCompletedIsFalseOrderByPriorityDesc(UUID assignedToId, LocalDate now);

    List<Task> findAllByAssignedToIdAndDueDateAfterAndCompletedIsFalseOrderByDueDateAscPriorityDesc(UUID assignedToId, LocalDate now);

    List<Task> findAllByAssignedToIdAndDueDateBeforeAndCompletedIsFalseOrderByPriorityDescDueDateAsc(UUID assignedToId, LocalDate now);

    List<Task> findAllByAssignedToIdAndCompletedIsFalseOrderByDueDateAscPriorityDesc(UUID assignedToId);

    List<Task> findAllByAssignedToId(UUID assignedToId);
}