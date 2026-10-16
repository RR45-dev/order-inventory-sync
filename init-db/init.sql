CREATE TABLE IF NOT EXISTS inventory_items (
    sku VARCHAR(64) PRIMARY KEY,
    description VARCHAR(255),
    quantity_on_hand INT NOT NULL,
    version BIGINT DEFAULT 0
);

INSERT INTO inventory_items (sku, description, quantity_on_hand, version) VALUES
    ('SKU-001', 'Wireless Mouse', 50, 0),
    ('SKU-002', 'Mechanical Keyboard', 30, 0),
    ('SKU-003', 'USB-C Hub', 15, 0)
ON CONFLICT (sku) DO NOTHING;
