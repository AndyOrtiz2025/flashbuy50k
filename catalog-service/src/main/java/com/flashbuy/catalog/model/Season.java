package com.flashbuy.catalog.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seasons")
@Data
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "season_id")
    private UUID seasonId;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}