package com.flashbuy.inventory_service.controller;

import com.flashbuy.inventory_service.model.Inventory;
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
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserve(@RequestBody Map<String, Object> body) {
        try {
            UUID productId = UUID.fromString(body.get("product_id").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());
            return ResponseEntity.ok(inventoryService.reserveStock(productId, quantity));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("LOCK_BUSY")) {
                return ResponseEntity.status(423)
                    .body(Map.of("error", "LOCK_BUSY", "message", e.getMessage()));
            }
            if (e.getMessage().contains("INSUFFICIENT_STOCK")) {
                return ResponseEntity.status(409)
                    .body(Map.of("error", "INSUFFICIENT_STOCK", "message", e.getMessage()));
            }
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody Map<String, Object> body) {
        try {
            UUID productId = UUID.fromString(body.get("product_id").toString());
            int quantity = Integer.parseInt(body.get("quantity").toString());
            return ResponseEntity.ok(inventoryService.releaseStock(productId, quantity));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }
}