-- ============================================
-- 测试数据
-- ============================================

-- 使用 INSERT IGNORE 避免重复插入
INSERT IGNORE INTO merchant (full_name, short_name, biz_type, currency, country, state, city, email, phone, status)
VALUES ('Demo Mart LLC', 'DemoMart', 1, 'USD', 'US', 'CA', 'Los Angeles', 'demo@demo.com', '1234567890', 1);

INSERT IGNORE INTO merchant (full_name, short_name, biz_type, currency, country, state, city, email, phone, status)
VALUES ('Qingo Test Inc', 'QingoTest', 2, 'USD', 'US', 'NY', 'New York', 'test@qingo.com', '0987654321', 1);

INSERT IGNORE INTO trade_order (order_no, merchant_id, shop_id, amount, currency, receive_type, status, payment_method)
VALUES ('ORD20240101001', 1, 1, 599, 'USD', 1, 1, 'WECHAT');

INSERT IGNORE INTO trade_order (order_no, merchant_id, shop_id, amount, currency, receive_type, status, payment_method)
VALUES ('ORD20240101002', 2, 2, 1299, 'USD', 2, 0, 'STRIPE');