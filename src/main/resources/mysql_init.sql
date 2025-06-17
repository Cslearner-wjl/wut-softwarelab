-- MySQL初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS d_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE d_trade;

-- 设置SQL模式，允许TIMESTAMP字段有默认值
SET SQL_MODE='ALLOW_INVALID_DATES';

-- 删除已存在的表（按照依赖关系倒序删除）
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    contact_info VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    trade_location VARCHAR(100),
    image_paths JSON,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    seller_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    receiver_id BIGINT NOT NULL,
    product_id BIGINT,
    order_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (student_id, username, password, nickname, role, enabled, create_time, update_time)
VALUES ('admin', '管理员', '$2a$10$yfIAUdMGQnQmKsZRQVLDPOK4CAvGXoX6Oa46XdQlDBn0Y/QIIvtlS', '系统管理员', 'ADMIN', true, NOW(), NOW());

-- 插入测试用户 (密码: password)
INSERT INTO users (student_id, username, password, nickname, contact_info, role, enabled, create_time, update_time)
VALUES 
('20210101', '张三', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '三哥', '13800138001', 'USER', true, NOW(), NOW()),
('20210102', '李四', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '四哥', '13800138002', 'USER', true, NOW(), NOW()),
('20210103', '王五', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '五哥', '13800138003', 'USER', true, NOW(), NOW());

-- 插入测试商品数据
INSERT INTO products (title, description, price, trade_location, image_paths, status, seller_id, create_time, update_time)
VALUES 
('全新iPad Pro', '2023年购入，95新，配件齐全，无拆修', 3999.00, '一号宿舍楼', JSON_ARRAY('uploads/images/ipad1.jpg', 'uploads/images/ipad2.jpg'), 'AVAILABLE', 2, NOW(), NOW()),
('Java编程思想（第4版）', '经典Java学习书籍，九成新', 45.00, '图书馆门口', JSON_ARRAY('uploads/images/book1.jpg'), 'AVAILABLE', 2, NOW(), NOW()),
('Nike运动鞋', '耐克运动鞋，42码，穿过几次，9成新', 199.00, '体育馆', JSON_ARRAY('uploads/images/shoes1.jpg', 'uploads/images/shoes2.jpg'), 'AVAILABLE', 3, NOW(), NOW()),
('机械键盘', '樱桃轴机械键盘，手感极佳', 299.00, '二号宿舍楼', JSON_ARRAY('uploads/images/keyboard1.jpg'), 'AVAILABLE', 4, NOW(), NOW()),
('自行车', '捷安特自行车，九成新，有锁', 599.00, '校门口自行车棚', JSON_ARRAY('uploads/images/bike1.jpg', 'uploads/images/bike2.jpg'), 'AVAILABLE', 3, NOW(), NOW());

-- 插入测试订单数据
INSERT INTO orders (product_id, buyer_id, seller_id, status, create_time, update_time)
VALUES 
(3, 4, 3, 'COMPLETED', NOW(), NOW()),
(4, 2, 4, 'PENDING', NOW(), NOW());

-- 插入测试消息数据
INSERT INTO messages (type, title, content, is_read, receiver_id, product_id, order_id, create_time)
VALUES 
('PRODUCT_INTEREST', '有人对您的商品感兴趣', '用户 王五 对您的商品 "机械键盘" 感兴趣，请及时联系。', false, 4, 4, 2, NOW()),
('ORDER_STATUS_CHANGE', '订单状态变更', '您的订单 #1 (Nike运动鞋) 已完成', true, 4, 3, 1, NOW()); 