CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    order_id UUID NOT NULL,

    user_id UUID NOT NULL,

    idempotency_key UUID NOT NULL UNIQUE,

    amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'FAILED',
                'REFUNDED'
            )
        ),

    payment_attempt INT NOT NULL DEFAULT 0,

    error_message TEXT,

    processed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order_id
ON payments(order_id);

CREATE UNIQUE INDEX idx_payments_idempotency_key
ON payments(idempotency_key);

CREATE INDEX idx_payments_user_id
ON payments(user_id);
