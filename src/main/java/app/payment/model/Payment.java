package app.payment.model;

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

    private String description;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private UUID accountId;
}
