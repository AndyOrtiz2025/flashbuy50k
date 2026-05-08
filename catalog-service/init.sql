CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE seasons (
    season_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(255) NOT NULL,

    start_date TIMESTAMPTZ NOT NULL,

    end_date TIMESTAMPTZ NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT true,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_season_dates
        CHECK (end_date > start_date)
);

CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    season_id UUID NOT NULL,

    name VARCHAR(255) NOT NULL,

    description TEXT,

    original_price NUMERIC(10,2) NOT NULL
        CHECK (original_price > 0),

    flash_price NUMERIC(10,2) NOT NULL
        CHECK (
            flash_price > 0
            AND flash_price <= original_price
        ),

    discount_pct NUMERIC(5,2)
        GENERATED ALWAYS AS (
            ROUND(
                ((original_price - flash_price)
                / original_price) * 100,
                2
            )
        ) STORED,

    is_visible BOOLEAN NOT NULL DEFAULT true,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_products_season
        FOREIGN KEY (season_id)
        REFERENCES seasons(season_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_products_season_id
ON products(season_id);

CREATE INDEX idx_products_visible
ON products(is_visible);

CREATE INDEX idx_seasons_active
ON seasons(is_active);
