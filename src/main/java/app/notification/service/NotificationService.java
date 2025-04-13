package app.notification.service;

import app.exception.NotificationServiceFeignCallException;
import app.notification.client.NotificationClient;
import app.notification.client.dto.Notification;
import app.notification.client.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    @Autowired
    public NotificationService(NotificationClient notificationClient) {

        this.notificationClient = notificationClient;
    }

    public List<Notification> getNotifications(UUID userId) {

        ResponseEntity<List<Notification>> httpResponse = notificationClient.getUserNotifications(userId);

        return httpResponse.getBody();
    }

    public void sendNotification(UUID userId, String email, String emailSubject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .topic(emailSubject)
                .body(emailBody)
                .email(email)
                .dateCreated(LocalDateTime.now())
                .build();

        ResponseEntity<Void> httpResponse;
        try {
            httpResponse = notificationClient.processNotification(notificationRequest);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Feign call to notification-svc failed. Unable to send notification to user with id [%s]".formatted(userId));
            }
        } catch (Exception e) {
            log.warn("Unable to send notification to user with id [%s] due to 500 Internal Server Error.".formatted(userId));
        }
    }

    public void archiveAll(UUID userId) {

        try {
            notificationClient.archiveUserNotifications(userId);
        } catch (Exception e) {
            log.error("Unable to access notification-svc to archive notification list for user with id [%s].".formatted(userId));
            throw new NotificationServiceFeignCallException("Unable to access notification-svc to archive notification list for user with id [%s].".formatted(userId));
        }
    }

    public long newNotificationsCount(UUID userId) {

        long count = 0;
        try {
            count = getNotifications(userId).stream()
                    .filter(Notification::isUnread)
                    .count();
        } catch (Exception e) {
            log.warn("Unable to access notification-svc to get new notifications count for user with id [%s].".formatted(userId));
        }

        return count;
    }

    public void markAllAsRead(UUID userId) {

        try {
            notificationClient.markAllAsRead(userId);
        } catch (Exception e) {
            log.error("Unable to access notification-svc to mark notifications as read for user with id [%s].".formatted(userId));
            throw new NotificationServiceFeignCallException("Unable to access notification-svc to mark notifications as read for user with id [%s].".formatted(userId));
        }
    }
}
