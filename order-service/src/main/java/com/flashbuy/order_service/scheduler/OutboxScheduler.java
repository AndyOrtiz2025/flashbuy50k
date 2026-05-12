package com.flashbuy.order_service.scheduler;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.flashbuy.order_service.model.OrderOutbox;
import com.flashbuy.order_service.repository.OrderOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.scheduler-delay:5000}")
    public void publishPendingEvents() {
        List<OrderOutbox> pending = outboxRepository.findUnpublishedEvents();

        for (OrderOutbox event : pending) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getPayload());
                event.setPublished(true);
                event.setPublishedAt(OffsetDateTime.now());
                outboxRepository.save(event);
                log.info("Evento publicado: {} para orden {}", event.getEventType(), event.getOrderId());
            } catch (Exception e) {
                log.error("Error publicando evento {}: {}", event.getOutboxId(), e.getMessage());
            }
        }
    }
}