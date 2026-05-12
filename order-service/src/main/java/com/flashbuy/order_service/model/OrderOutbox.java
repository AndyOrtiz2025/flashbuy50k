package com.flashbuy.order_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_outbox")
@Data
public class OrderOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "outbox_id")
    private UUID outboxId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "published", nullable = false)
    private Boolean published = false;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}