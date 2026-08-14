-- Day11 消费者幂等：orders.order_no 唯一索引
ALTER TABLE orders ADD UNIQUE INDEX uk_order_no (order_no);
