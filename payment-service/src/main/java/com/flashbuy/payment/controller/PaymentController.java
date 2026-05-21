package com.flashbuy.payment.controller;

import com.flashbuy.payment.model.Payment;
import com.flashbuy.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "payment-service"));
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // Validar header X-User-Id
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Header X-User-Id es obligatorio"));
        }

        // Validar campos obligatorios
        if (body.get("idempotency_key") == null || body.get("order_id") == null || body.get("amount") == null) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "idempotency_key, order_id y amount son obligatorios"));
        }

        try {
            UUID idempotencyKey = UUID.fromString(body.get("idempotency_key").toString());
            UUID orderId = UUID.fromString(body.get("order_id").toString());
            BigDecimal amount = new BigDecimal(body.get("amount").toString());

            // Validar amount positivo
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(422)
                    .body(Map.of("error", "UNPROCESSABLE_ENTITY", "message", "amount debe ser mayor a 0"));
            }

            // Idempotencia — si ya existe, devuelve el mismo resultado
            var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return ResponseEntity.status(409)
                    .body(Map.of(
                        "error", "DUPLICATE_PAYMENT",
                        "payment_id", existing.get().getPaymentId(),
                        "status", existing.get().getStatus()
                    ));
            }

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setUserId(UUID.fromString(userId));
            payment.setIdempotencyKey(idempotencyKey);
            payment.setAmount(amount);
            payment.setStatus("APPROVED");
            return ResponseEntity.status(201).body(paymentRepository.save(payment));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "BAD_REQUEST", "message", "Formato inválido en los datos enviados"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "INTERNAL_ERROR", "message", e.getMessage()));
        }
    }
}