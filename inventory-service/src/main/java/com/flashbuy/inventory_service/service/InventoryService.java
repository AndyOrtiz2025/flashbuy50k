package com.flashbuy.inventory_service.service;

import com.flashbuy.inventory_service.model.Inventory;
import com.flashbuy.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public Inventory reserveStock(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

        if (inventory.getAvailableStock() < quantity) {
            throw new RuntimeException("Stock insuficiente");
        }

        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory releaseStock(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventory.setReservedStock(inventory.getReservedStock() - quantity);

        return inventoryRepository.save(inventory);
    }

    public Inventory getByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));
    }
}