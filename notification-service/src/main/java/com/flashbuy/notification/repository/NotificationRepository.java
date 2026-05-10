package com.flashbuy.notification.repository;

import com.flashbuy.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends 
JpaRepository<Notification, UUID> {

    List<Notification> findByUserId(UUID userId);
}
