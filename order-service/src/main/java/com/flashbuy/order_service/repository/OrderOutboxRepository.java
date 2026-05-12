package com.flashbuy.order_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.flashbuy.order_service.model.OrderOutbox;

public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, UUID> {

    @Query("SELECT o FROM OrderOutbox o WHERE o.published = false ORDER BY o.createdAt ASC")
    List<OrderOutbox> findUnpublishedEvents();
}