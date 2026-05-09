package com.flashbuy.catalog.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "flash_price", nullable = false)
    private BigDecimal flashPrice;

    @Column(name = "discount_pct", insertable = false, updatable = false)
    private BigDecimal discountPct;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}