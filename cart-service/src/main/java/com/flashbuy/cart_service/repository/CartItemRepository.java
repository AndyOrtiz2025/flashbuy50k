package com.flashbuy.cart_service.repository;

import com.flashbuy.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByUserIdAndStatus(UUID userId, String status);
}