-- 先查看外键并删除
SET FOREIGN_KEY_CHECKS = 0;

-- 重新创建products表
DROP TABLE IF EXISTS product_images;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    trade_location VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    seller_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status)
);

-- 创建product_images表
CREATE TABLE product_images (
    product_id BIGINT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (product_id, image_path),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1; 