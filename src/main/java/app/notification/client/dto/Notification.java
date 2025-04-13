package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class Notification {

    private String topic;

    private String body;

    private LocalDateTime dateCreated;

    private boolean unread;
}