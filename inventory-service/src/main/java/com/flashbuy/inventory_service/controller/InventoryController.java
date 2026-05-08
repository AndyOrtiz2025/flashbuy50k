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
    public ResponseEntity<Inventory> getStock(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<Inventory> reserve(@RequestBody Map<String, Object> body) {
        UUID productId = UUID.fromString(body.get("product_id").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return ResponseEntity.ok(inventoryService.reserveStock(productId, quantity));
    }

    @PostMapping("/release")
    public ResponseEntity<Inventory> release(@RequestBody Map<String, Object> body) {
        UUID productId = UUID.fromString(body.get("product_id").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return ResponseEntity.ok(inventoryService.releaseStock(productId, quantity));
    }
}