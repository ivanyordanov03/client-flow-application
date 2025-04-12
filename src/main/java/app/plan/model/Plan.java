package app.plan.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private PlanName planName;

    @Column(nullable = false)
    private BigDecimal pricePerMonth;

    @Column(nullable = false)
    private int maxUsers;
}
