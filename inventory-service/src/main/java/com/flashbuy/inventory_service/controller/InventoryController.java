package com.flashbuy.inventory_service.controller;

import com.flashbuy.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "inventory-service"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getStock(@PathVariable UUID productId) {
        try {
            return ResponseEntity.ok(inventoryService.getByProductId(productId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", "NOT_FOUND", "message", e.getMessage()));
        }
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserve(@RequestBody Map<String, Object> body) {

        // Validar campos obligatorios
        if (body.get("product_id") == null || body.get("quantity") == null) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "product_id y quantity son obligatorios"));
        }

        try {
            UUID productId = UUID.fromString(body.get("product_id").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());

            // Validar cantidad positiva
            if (quantity <= 0) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "quantity debe ser mayor a 0"));
            }

            // Validar que no sea un número extremadamente grande
            if (quantity > 100000) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "quantity excede el límite permitido"));
            }

            return ResponseEntity.ok(inventoryService.reserveStock(productId, quantity));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato inválido en los datos enviados"));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("LOCK_BUSY")) {
                return ResponseEntity.status(423)
                    .body(Map.of("error", "LOCK_BUSY", "message", message));
            }
            if (message != null && message.contains("INSUFFICIENT_STOCK")) {
                return ResponseEntity.status(409)
                    .body(Map.of("error", "INSUFFICIENT_STOCK", "message", "Stock insuficiente"));
            }
            if (message != null && message.contains("Producto no encontrado")) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "NOT_FOUND", "message", message));
            }
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", message));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody Map<String, Object> body) {

        // Validar campos obligatorios
        if (body.get("product_id") == null || body.get("quantity") == null) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "product_id y quantity son obligatorios"));
        }

        try {
            UUID productId = UUID.fromString(body.get("product_id").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());

            // Validar cantidad positiva
            if (quantity <= 0) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "quantity debe ser mayor a 0"));
            }

            return ResponseEntity.ok(inventoryService.releaseStock(productId, quantity));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato inválido en los datos enviados"));
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("Producto no encontrado")) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "NOT_FOUND", "message", message));
            }
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", message));
        }
    }
}