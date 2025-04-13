package app.event.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InAppNotificationEvent {

    @NotNull
    private UUID userId;

    @NotBlank
    private String topic;

    @NotBlank
    private String body;

    @NotBlank
    private LocalDateTime dateCreated;
}
