package app.client.model;

import app.account.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.UUID;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Client {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;
}
