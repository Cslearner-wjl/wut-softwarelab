-- V1__Initial_Schema.sql
-- 数据库初始化脚本

-- 创建用户表
CREATE TABLE users
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    nickname     VARCHAR(255),
    contact_info VARCHAR(255),
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    role         VARCHAR(255) NOT NULL DEFAULT 'USER',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品表
CREATE TABLE products
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255)   NOT NULL,
    price          DECIMAL(10, 2) NOT NULL,
    description    TEXT,
    trade_location VARCHAR(255),
    status         VARCHAR(255)   NOT NULL DEFAULT 'AVAILABLE',
    seller_id      BIGINT         NOT NULL,
    create_time    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- 创建商品图片表
CREATE TABLE product_images
(
    product_id BIGINT       NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- 创建订单表
CREATE TABLE orders
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT       NOT NULL,
    buyer_id   BIGINT       NOT NULL,
    seller_id  BIGINT       NOT NULL,
    status     VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (buyer_id) REFERENCES users (id),
    FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- 创建消息表
CREATE TABLE messages
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    type        VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    content     TEXT,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    receiver_id BIGINT       NOT NULL,
    product_id  BIGINT,
    order_id    BIGINT,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receiver_id) REFERENCES users (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- 插入初始数据

-- 密码是 'password' 的BCrypt哈希值: $2a$10$AbJ2Jd.tY3.m4A5iJv5j9.2Q7M/b1Qf/5W2nB9X.XVzL.8T6G.S4K
-- 请在实际应用中使用后端生成的密码哈希
INSERT INTO users (student_id, username, password, nickname, contact_info, role)
VALUES ('admin', 'admin', '$2a$10$AbJ2Jd.tY3.m4A5iJv5j9.2Q7M/b1Qf/5W2nB9X.XVzL.8T6G.S4K', '管理员', 'admin@example.com',
        'ADMIN'),
       ('1001', 'user1', '$2a$10$AbJ2Jd.tY3.m4A5iJv5j9.2Q7M/b1Qf/5W2nB9X.XVzL.8T6G.S4K', '张三', 'zhangsan@example.com',
        'USER'),
       ('1002', 'user2', '$2a$10$AbJ2Jd.tY3.m4A5iJv5j9.2Q7M/b1Qf/5W2nB9X.XVzL.8T6G.S4K', '李四', 'lisi@example.com',
        'USER');

-- 插入商品数据 (由用户2 - 李四发布)
INSERT INTO products (title, price, description, trade_location, seller_id)
VALUES ('九成新二手吉他', 350.00, 'YAMAHA F310，音色优美，几乎全新，附赠吉他包和拨片。', '大学生活动中心', 3),
       ('考研数学资料全套', 80.00, '全套考研数学复习资料，包含教材、习题集和历年真题，笔记完整。', '图书馆门口', 3),
       ('品牌无线蓝牙耳机', 120.00, '音质很好，续航能力强，兼容各种设备。', '线上交易', 3);

-- 为商品插入图片
INSERT INTO product_images (product_id, image_path)
VALUES (1, '/uploads/guitar1.jpg'),
       (1, '/uploads/guitar2.jpg'),
       (2, '/uploads/math_books.jpg'),
       (3, '/uploads/earphone.jpg');

-- 插入订单数据 (用户1 - 张三 购买了 用户2 - 李四 的吉他)
INSERT INTO orders (product_id, buyer_id, seller_id, status)
VALUES (1, 2, 3, 'PENDING');

-- 插入消息数据 (通知卖家李四，有人对他的吉他感兴趣)
INSERT INTO messages (type, title, content, receiver_id, product_id, order_id)
VALUES ('PRODUCT_INTEREST', '您的商品有人感兴趣', '用户 张三 对您的商品 "九成新二手吉他" 很感兴趣，并创建了交易订单。', 3, 1, 1); 