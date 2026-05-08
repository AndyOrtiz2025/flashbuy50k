CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE inventory (
    inventory_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    product_id UUID NOT NULL UNIQUE,

    total_stock INT NOT NULL CHECK (total_stock >= 0),

    available_stock INT NOT NULL CHECK (available_stock >= 0),

    reserved_stock INT NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),

    version INT NOT NULL DEFAULT 0,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_stock_balance
        CHECK (available_stock + reserved_stock <= total_stock)
);

CREATE UNIQUE INDEX idx_inventory_product_id
ON inventory(product_id);
