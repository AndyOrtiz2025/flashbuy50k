package com.flashbuy.payment.event;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class StockReservedEvent {
    private UUID orderId;
    private UUID userId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal amount;
    private UUID idempotencyKey;
}