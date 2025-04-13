package app.event;

import app.event.payload.InAppNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InAppNotificationEventPublisher {

    private final KafkaTemplate<String, InAppNotificationEvent> kafkaTemplate;

    public InAppNotificationEventPublisher(KafkaTemplate<String, InAppNotificationEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(InAppNotificationEvent event) {

        kafkaTemplate.send("in-app-notification-event.v1", event);
        log.info("Successfully published notification event for user with id [%s]".formatted(event.getUserId()));
    }
}
