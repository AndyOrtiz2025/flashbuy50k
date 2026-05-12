package com.flashbuy.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashbuy.payment.event.StockReservedEvent;
import com.flashbuy.payment.model.Payment;
import com.flashbuy.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock.reserved", groupId = "payment-service")
    public void processPayment(String message) {
        try {
            StockReservedEvent event = objectMapper.readValue(message, StockReservedEvent.class);

            if (paymentRepository.findByIdempotencyKey(event.getIdempotencyKey()).isPresent()) {
                log.info("Pago duplicado ignorado para idempotency_key: {}", event.getIdempotencyKey());
                return;
            }

            boolean paymentSuccess = simulatePayment();

            Payment payment = new Payment();
            payment.setOrderId(event.getOrderId());
            payment.setUserId(event.getUserId());
            payment.setIdempotencyKey(event.getIdempotencyKey());
            payment.setAmount(event.getAmount());
            payment.setPaymentAttempt(1);

            if (paymentSuccess) {
                payment.setStatus("APPROVED");
                payment.setProcessedAt(OffsetDateTime.now());
                paymentRepository.save(payment);

                kafkaTemplate.send("payment.completed", objectMapper.writeValueAsString(Map.of(
                    "order_id", event.getOrderId(),
                    "user_id", event.getUserId(),
                    "status", "APPROVED"
                )));
                log.info("Pago aprobado para orden: {}", event.getOrderId());

            } else {
                payment.setStatus("FAILED");
                payment.setErrorMessage("Pago rechazado por la pasarela");
                paymentRepository.save(payment);

                kafkaTemplate.send("payment.failed", objectMapper.writeValueAsString(Map.of(
                    "order_id", event.getOrderId(),
                    "product_id", event.getProductId(),
                    "quantity", event.getQuantity(),
                    "reason", "PAYMENT_REJECTED"
                )));
                log.warn("Pago fallido para orden: {} — iniciando compensación Saga", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("Error procesando pago: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean simulatePayment() {
        return Math.random() > 0.1;
    }
}