package com.flashbuy.inventory_service.service;

import com.flashbuy.inventory_service.model.Inventory;
import com.flashbuy.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:inventory:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    @Transactional
    public Inventory reserveStock(UUID productId, int quantity) {

        // CAPA 1 — Lock Redis (TTL 5 segundos)
        String lockKey = LOCK_PREFIX + productId;
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "LOCKED", LOCK_TTL);

        if (Boolean.FALSE.equals(acquired)) {
            throw new RuntimeException("LOCK_BUSY: Producto siendo modificado por otro proceso");
        }

        try {
            // CAPA 2 — Concurrencia optimista con version
            Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

            if (inventory.getAvailableStock() < quantity) {
                throw new RuntimeException("INSUFFICIENT_STOCK: Stock insuficiente");
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
            inventory.setReservedStock(inventory.getReservedStock() + quantity);

            // CAPA 3 — CHECK constraint en BD garantiza available_stock >= 0
            return inventoryRepository.save(inventory);

        } finally {
            // Siempre liberar el lock
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public Inventory releaseStock(UUID productId, int quantity) {
        String lockKey = LOCK_PREFIX + productId;
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "LOCKED", LOCK_TTL);

        if (Boolean.FALSE.equals(acquired)) {
            throw new RuntimeException("LOCK_BUSY: Producto siendo modificado por otro proceso");
        }

        try {
            Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

            inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
            inventory.setReservedStock(inventory.getReservedStock() - quantity);

            return inventoryRepository.save(inventory);

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public Inventory getByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));
    }
}