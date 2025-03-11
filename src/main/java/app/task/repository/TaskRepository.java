package app.task.repository;

import app.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByAccountIdAndAssignedToId(UUID accountId, UUID assignedToUserId);

    List<Task> findAllByAccountId(UUID accountId);
}
