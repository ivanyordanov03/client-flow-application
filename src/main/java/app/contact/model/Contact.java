package app.contact.model;

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
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;

    private String businessName;

    private String address;

    private String phoneNumber;

    @Column(nullable = false)
    private UUID assignedToId;

    @Column(nullable = false)
    private String assignedToName;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    private LocalDateTime dateUpdated;
}
