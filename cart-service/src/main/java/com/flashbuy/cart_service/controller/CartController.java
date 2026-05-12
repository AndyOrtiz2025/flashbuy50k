package com.flashbuy.cart_service.controller;

import com.flashbuy.cart_service.model.CartItem;
import com.flashbuy.cart_service.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "cart-service"));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItem> addItem(@RequestBody Map<String, Object> body,
                                             @RequestHeader("X-User-Id") String userId) {
        CartItem item = new CartItem();
        item.setUserId(UUID.fromString(userId));
        item.setProductId(UUID.fromString(body.get("product_id").toString()));
        item.setQuantity(Integer.parseInt(body.get("quantity").toString()));
        item.setUnitPrice(new BigDecimal(body.get("unit_price").toString()));
        return ResponseEntity.status(201).body(cartItemRepository.save(item));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItem>> getItems(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
            cartItemRepository.findByUserIdAndStatus(UUID.fromString(userId), "active")
        );
    }
}