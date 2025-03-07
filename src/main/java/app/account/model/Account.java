package app.account.model;

import app.client.model.Client;
import app.payment.model.Payment;
import app.paymentMethod.model.PaymentMethod;
import app.plan.model.Plan;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private String phoneNumber;

    @Column(nullable = false)
    private boolean isActive = false;

    @ManyToOne
    private Plan plan;

    @Column(nullable = false)
    private boolean isAutoRenewalEnabled = false;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    List<Payment> paymentHistory = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    List<PaymentMethod> paymentMethods = new ArrayList<>();

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    List<Client> clients = new ArrayList<>();

    @OneToOne
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
