package com.flashbuy.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flashbuy.catalog.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findBySeasonIdAndIsVisibleTrue(UUID seasonId);
}