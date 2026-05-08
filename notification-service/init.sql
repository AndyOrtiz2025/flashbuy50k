CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    order_id UUID,

    type VARCHAR(50) NOT NULL
        CHECK (
            type IN (
                'ORDER_CONFIRMED',
                'ORDER_FAILED',
                'PAYMENT_APPROVED',
                'PAYMENT_FAILED'
            )
        ),

    message TEXT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'SENT',
                'FAILED'
            )
        ),

    retry_count INT NOT NULL DEFAULT 0
        CHECK (retry_count >= 0),

    sent_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id
ON notifications(user_id);

CREATE INDEX idx_notifications_status
ON notifications(status);

CREATE INDEX idx_notifications_order_id
ON notifications(order_id);
