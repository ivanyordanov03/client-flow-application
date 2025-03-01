package app.account.model;

import app.plan.model.Plan;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String businessName;

    private String address;

    private String companyLogo;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean isActive = false;

    @ManyToOne
    private Plan plan;

    @Column(nullable = false)
    private boolean isAutoRenewalEnabled = false;

    @OneToMany
    List<User> userList;

    @OneToOne
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
