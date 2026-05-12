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
    public ResponseEntity<Payment> process(@RequestBody Map<String, Object> body,
                                            @RequestHeader("X-User-Id") String userId) {
        UUID idempotencyKey = UUID.fromString(body.get("idempotency_key").toString());

        // Idempotencia — si ya existe, devuelve el mismo resultado
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
            .map(existing -> ResponseEntity.ok(existing))
            .orElseGet(() -> {
                Payment payment = new Payment();
                payment.setOrderId(UUID.fromString(body.get("order_id").toString()));
                payment.setUserId(UUID.fromString(userId));
                payment.setIdempotencyKey(idempotencyKey);
                payment.setAmount(new BigDecimal(body.get("amount").toString()));
                payment.setStatus("APPROVED"); // Simulado por ahora
                return ResponseEntity.status(201).body(paymentRepository.save(payment));
            });
    }
}
