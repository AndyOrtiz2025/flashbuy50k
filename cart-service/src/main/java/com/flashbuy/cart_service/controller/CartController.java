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
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> body,
                                     @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // Validar header X-User-Id
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Header X-User-Id es obligatorio"));
        }

        // Validar campos obligatorios
        if (body.get("product_id") == null || body.get("quantity") == null || body.get("unit_price") == null) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "product_id, quantity y unit_price son obligatorios"));
        }

        try {
            int quantity = Integer.parseInt(body.get("quantity").toString());

            // Validar cantidad positiva
            if (quantity <= 0) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "quantity debe ser mayor a 0"));
            }

            BigDecimal unitPrice = new BigDecimal(body.get("unit_price").toString());

            // Validar precio positivo
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "unit_price debe ser mayor a 0"));
            }

            CartItem item = new CartItem();
            item.setUserId(UUID.fromString(userId));
            item.setProductId(UUID.fromString(body.get("product_id").toString()));
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            return ResponseEntity.status(201).body(cartItemRepository.save(item));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato inválido en los datos enviados"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> getItems(@RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Header X-User-Id es obligatorio"));
        }

        try {
            return ResponseEntity.ok(
                cartItemRepository.findByUserIdAndStatus(UUID.fromString(userId), "active")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato de X-User-Id inválido"));
        }
    }
}
