package app.account.model;

import app.plan.model.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    private String phoneNumber;

    @Column(nullable = false)
    private boolean isActive = false;

    @ManyToOne
    private Plan plan;

    @Column(nullable = false)
    private boolean isAutoRenewalEnabled = false;

    @Column(nullable = false)
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    private LocalDateTime dateUpdated;

    @Column(nullable = false)
    private LocalDateTime dateExpiring;

    private UUID ownerId;
}
