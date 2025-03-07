package app.task.model;

import app.account.model.Account;
import app.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Task {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Size(max = 1500)
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    private Priority priority;

    @ManyToOne
    private Account account;

    @ManyToOne
    private User assignedTo;
}
