package com.flashmall.service;

import com.flashmall.dto.OrderMessageDTO;
import com.flashmall.entity.Order;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderVO;

import java.util.List;


public interface OrderService {

    /**
     * 提交订单（异步下单入口）
     * Redis 扣库存 + 发送 RabbitMQ 消息
     *
     * @return 订单号（orderNo）
     */
    String submitOrder(Long productId, Integer quantity);

    /**
     * 消费者调用：根据消息创建数据库订单
     */
    Order createOrderByMessage(OrderMessageDTO message);

    List<OrderVO> getMyOrders();

    OrderDetailVO getOrderDetail(Long orderId);

    void pay(Long orderId);

    void cancel(Long orderId);

    void cancelTimeoutOrders();
}
