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

    @Column(nullable = false, unique = true)
    private String domainName;

    private String businessName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    private String companyLogo;

    @Column(nullable = false)
    public boolean isActive = false;

    @ManyToOne
    private Plan plan;

    @Column(nullable = false)
    public boolean isAutoRenewalEnabled = false;

    @OneToMany
    List<User> userList;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

}
