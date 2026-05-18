-- ============================================
-- ORDER SERVICE — Base de datos: order_db
-- ============================================

CREATE TABLE IF NOT EXISTS orders (
    order_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    idempotency_key     UUID NOT NULL UNIQUE,
    total_amount        NUMERIC(10,2) NOT NULL CHECK (total_amount > 0),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN (
                            'PENDING',
                            'CONFIRMED',
                            'FAILED',
                            'CANCELLED'
                        )),
    saga_state          JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    item_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id      UUID NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10,2) NOT NULL CHECK (unit_price > 0)
);

CREATE TABLE IF NOT EXISTS order_outbox (
    outbox_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    event_type      VARCHAR(50) NOT NULL,
    payload         JSONB NOT NULL,
    published       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE UNIQUE INDEX idx_orders_idempotency_key ON orders(idempotency_key);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_outbox_published ON order_outbox(published) WHERE published = false;
```
