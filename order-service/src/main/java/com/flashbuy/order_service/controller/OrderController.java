package com.flashbuy.order_service.controller;

import com.flashbuy.order_service.model.Order;
import com.flashbuy.order_service.model.OrderOutbox;
import com.flashbuy.order_service.repository.OrderOutboxRepository;
import com.flashbuy.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "order-service"));
    }

    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<?> confirm(@RequestBody Map<String, Object> body,
                                     @RequestHeader("X-User-Id") String userId) {

        // Validar campos obligatorios
        if (body.get("idempotency_key") == null || body.get("total_amount") == null) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "idempotency_key y total_amount son obligatorios"));
        }

        try {
            UUID idempotencyKey = UUID.fromString(body.get("idempotency_key").toString());

            // IDEMPOTENCIA — si ya existe, devuelve la misma orden
            var existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return ResponseEntity.status(409)
                    .body(Map.of("error", "DUPLICATE_ORDER", "order_id", existing.get().getOrderId()));
            }

            // Crear orden
            Order order = new Order();
            order.setUserId(UUID.fromString(userId));
            order.setIdempotencyKey(idempotencyKey);
            order.setTotalAmount(new BigDecimal(body.get("total_amount").toString()));
            order.setStatus("PENDING");
            Order saved = orderRepository.save(order);

            // OUTBOX PATTERN — escribir evento en la misma transacción
            OrderOutbox outbox = new OrderOutbox();
            outbox.setOrderId(saved.getOrderId());
            outbox.setEventType("order.created");
            outbox.setPayload(objectMapper.writeValueAsString(Map.of(
                "order_id", saved.getOrderId(),
                "user_id", userId,
                "total_amount", saved.getTotalAmount(),
                "status", "PENDING"
            )));
            outboxRepository.save(outbox);

            return ResponseEntity.status(201).body(saved);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato de UUID inválido"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable UUID orderId) {
        return orderRepository.findById(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}