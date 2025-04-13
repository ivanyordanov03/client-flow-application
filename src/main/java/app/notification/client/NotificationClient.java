package app.notification.client;

import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.Notification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-svc", url = "${notification-svc.base-url}")
public interface NotificationClient {

    @PostMapping
    ResponseEntity<Void> processNotification(@RequestBody NotificationRequest notificationRequest);

    @GetMapping
    ResponseEntity<List<Notification>> getUserNotifications(@RequestParam(name = "userId") UUID userId);

    @DeleteMapping
    ResponseEntity<Void> archiveUserNotifications(@RequestParam(name = "userId") UUID userId);

    @PutMapping
    ResponseEntity<Void> markAllAsRead(@RequestParam(name = "userId") UUID userId);
}
