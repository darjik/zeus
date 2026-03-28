-- Create purchase_orders table
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    po_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    INDEX idx_po_order_id (order_id),
    INDEX idx_po_vendor_id (vendor_id),
    INDEX idx_po_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create purchase_order_items table
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2),
    total_price DECIMAL(15,2),
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    INDEX idx_poi_purchase_order_id (purchase_order_id),
    INDEX idx_poi_order_item_id (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add product_status column to order_items table
ALTER TABLE order_items
ADD COLUMN IF NOT EXISTS product_status VARCHAR(50) DEFAULT 'PENDING' AFTER notes;

-- Create order_item_parts table for customizable parts per order item
CREATE TABLE IF NOT EXISTS order_item_parts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id BIGINT NOT NULL,
    product_part_id BIGINT NOT NULL,
    quantity_required INT,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (product_part_id) REFERENCES product_parts(id),
    INDEX idx_oip_order_item_id (order_item_id),
    INDEX idx_oip_product_part_id (product_part_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


