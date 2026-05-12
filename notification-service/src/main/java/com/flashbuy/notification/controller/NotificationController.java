package com.flashbuy.notification.controller;

import com.flashbuy.notification.model.Notification;
import com.flashbuy.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notification-service"));
    }

    @PostMapping("/send")
    public ResponseEntity<Notification> send(@RequestBody Map<String, Object> body) {
        Notification notification = new Notification();
        notification.setUserId(UUID.fromString(body.get("user_id").toString()));
        notification.setOrderId(UUID.fromString(body.get("order_id").toString()));
        notification.setEventSource(body.get("event_source").toString());
        notification.setType(body.get("type").toString());
        notification.setStatus("SENT");

        return ResponseEntity.status(201).body(notificationRepository.save(notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationRepository.findByUserId(userId));
    }
}
