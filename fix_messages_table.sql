-- 重新创建messages表
DROP TABLE IF EXISTS messages;

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    receiver_id BIGINT NOT NULL,
    product_id BIGINT,
    order_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_product_id (product_id),
    INDEX idx_order_id (order_id)
); 