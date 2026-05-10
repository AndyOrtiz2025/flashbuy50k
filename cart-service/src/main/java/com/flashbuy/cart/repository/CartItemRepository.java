package com.flashbuy.cart.repository;

import com.flashbuy.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> 
{

    List<CartItem> findByUserId(UUID userId);
}
