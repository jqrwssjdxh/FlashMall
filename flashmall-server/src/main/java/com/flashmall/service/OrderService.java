package com.flashmall.service;

import com.flashmall.entity.Order;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderVO;

import java.util.List;


public interface OrderService {


    /**
     * 创建订单
     */
    Order createOrder(Long productId, Integer quantity);

    List<OrderVO> getMyOrders();

    OrderDetailVO getOrderDetail(Long orderId);
}