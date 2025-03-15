package app.task.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "VARCHAR(1000)")
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate completedOn;

    private TaskPriority priority; // 1 - Low, 2 - Medium, 3 - High, 4 - Urgent

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID assignedToId;

    @Column(nullable = false)
    private String assignedToName;

    @Column(nullable = false)
    private UUID createdById;

    @Column(nullable = false)
    private String createdByName;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

}
