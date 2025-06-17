USE d_trade;

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (student_id, username, password, nickname, role, enabled)
VALUES ('admin', '管理员', '$2a$10$yfIAUdMGQnQmKsZRQVLDPOK4CAvGXoX6Oa46XdQlDBn0Y/QIIvtlS', '系统管理员', 'ADMIN', true);

-- 插入测试用户 (密码: password)
INSERT INTO users (student_id, username, password, nickname, contact_info, role, enabled)
VALUES 
('20210101', '张三', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '三哥', '13800138001', 'USER', true),
('20210102', '李四', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '四哥', '13800138002', 'USER', true),
('20210103', '王五', '$2a$10$8CU8yBnEJKdSFnIk7RNOhes9nI5CJ0Os.WyUr1thcUgHG9ZNrXe/C', '五哥', '13800138003', 'USER', true);

-- 插入测试商品数据
INSERT INTO products (title, description, price, trade_location, image_paths, status, seller_id)
VALUES 
('全新iPad Pro', '2023年购入，95新，配件齐全，无拆修', 3999.00, '一号宿舍楼', '["uploads/images/ipad1.jpg", "uploads/images/ipad2.jpg"]', 'AVAILABLE', 2),
('Java编程思想（第4版）', '经典Java学习书籍，九成新', 45.00, '图书馆门口', '["uploads/images/book1.jpg"]', 'AVAILABLE', 2),
('Nike运动鞋', '耐克运动鞋，42码，穿过几次，9成新', 199.00, '体育馆', '["uploads/images/shoes1.jpg", "uploads/images/shoes2.jpg"]', 'AVAILABLE', 3),
('机械键盘', '樱桃轴机械键盘，手感极佳', 299.00, '二号宿舍楼', '["uploads/images/keyboard1.jpg"]', 'AVAILABLE', 4),
('自行车', '捷安特自行车，九成新，有锁', 599.00, '校门口自行车棚', '["uploads/images/bike1.jpg", "uploads/images/bike2.jpg"]', 'AVAILABLE', 3);

-- 插入测试订单数据
INSERT INTO orders (product_id, buyer_id, seller_id, status)
VALUES 
(3, 4, 3, 'COMPLETED'),
(4, 2, 4, 'PENDING');

-- 插入测试消息数据
INSERT INTO messages (type, title, content, is_read, receiver_id, product_id, order_id)
VALUES 
('PRODUCT_INTEREST', '有人对您的商品感兴趣', '用户 王五 对您的商品 "机械键盘" 感兴趣，请及时联系。', false, 4, 4, 2),
('ORDER_STATUS_CHANGE', '订单状态变更', '您的订单 #1 (Nike运动鞋) 已完成', true, 4, 3, 1); 