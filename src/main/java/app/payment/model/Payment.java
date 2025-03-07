package app.payment.model;

import app.account.model.Account;
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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false)
    private String last4Digits;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    private Account account;
}
