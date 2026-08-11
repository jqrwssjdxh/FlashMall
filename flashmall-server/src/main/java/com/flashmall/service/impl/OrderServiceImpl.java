package com.flashmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flashmall.common.UserContext;
import com.flashmall.constant.OrderStatus;
import com.flashmall.constant.ResultCode;
import com.flashmall.entity.Order;
import com.flashmall.entity.OrderItem;
import com.flashmall.entity.Product;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.OrderItemMapper;
import com.flashmall.mapper.OrderMapper;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.service.OrderService;
import com.flashmall.service.StockService;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderItemVO;
import com.flashmall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;

    @Override
    @Transactional
    public Order createOrder(Long productId, Integer quantity) {
        Long userId = getUserIdOrThrow();

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 1. Redis Lua 原子扣减库存（第一道防线，抗高并发）
        boolean success = stockService.decreaseStock(productId, quantity);
        if (!success) {
            throw new BusinessException(ResultCode.OUT_OF_STOCK);
        }

        // 2. MySQL 条件更新扣减库存（最终数据源，兜底防超卖）
        int rows = productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .ge(Product::getStock, quantity)
                        .setSql("stock = stock - " + quantity)
        );
        if (rows == 0) {
            throw new BusinessException(ResultCode.OUT_OF_STOCK);
        }

        // 3. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(OrderStatus.WAIT_PAY.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 创建订单明细
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        orderItemMapper.insert(item);

        return order;
    }

    @Override
    public List<OrderVO> getMyOrders() {
        Long userId = getUserIdOrThrow();

        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
        );

        return orders.stream().map(OrderVO::from).collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        Long userId = getUserIdOrThrow();

        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getUserId, userId)
        );
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
        );

        List<OrderItemVO> itemVOList = items.stream().map(item -> {
            OrderItemVO vo = new OrderItemVO();
            vo.setProductId(item.getProductId());
            vo.setProductName(item.getProductName());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            return vo;
        }).toList();

        return OrderDetailVO.from(order, itemVOList);
    }

    @Override
    @Transactional
    public void pay(Long orderId) {


        Long userId = getUserIdOrThrow();


        Order order =
                orderMapper.selectOne(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getId,orderId)
                                .eq(Order::getUserId,userId)
                );


        if(order == null){

            throw new BusinessException(
                    ResultCode.ORDER_NOT_FOUND
            );

        }


        if(!OrderStatus.WAIT_PAY.getCode()
                .equals(order.getStatus())){


            throw new BusinessException(
                    ResultCode.ORDER_STATUS_ERROR
            );

        }


        order.setStatus(
                OrderStatus.PAID.getCode()
        );


        order.setUpdateTime(
                LocalDateTime.now()
        );


        orderMapper.updateById(order);

    }

    @Override
    @Transactional
    public void cancel(Long orderId) {


        Long userId = getUserIdOrThrow();


        Order order =
                orderMapper.selectOne(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getId, orderId)
                                .eq(Order::getUserId, userId)
                );


        if(order == null){

            throw new BusinessException(
                    ResultCode.ORDER_NOT_FOUND
            );

        }


        // 只能取消待支付订单
        if(!OrderStatus.WAIT_PAY.getCode()
                .equals(order.getStatus())){


            throw new BusinessException(
                    ResultCode.ORDER_STATUS_ERROR
            );

        }


        order.setStatus(
                OrderStatus.CANCELLED.getCode()
        );


        order.setUpdateTime(
                LocalDateTime.now()
        );


        orderMapper.updateById(order);

    }

    @Override
    @Transactional
    public void cancelTimeoutOrders() {

        log.info("开始扫描超时订单...");

        LocalDateTime timeout =
                LocalDateTime.now()
                        .minusMinutes(30);


        List<Order> orders =
                orderMapper.selectList(
                        new LambdaQueryWrapper<Order>()
                                .eq(
                                        Order::getStatus,
                                        OrderStatus.WAIT_PAY.getCode()
                                )
                                .lt(
                                        Order::getCreateTime,
                                        timeout
                                )
                );


        for(Order order: orders){


            order.setStatus(
                    OrderStatus.CANCELLED.getCode()
            );


            order.setUpdateTime(
                    LocalDateTime.now()
            );


            orderMapper.updateById(order);

        }

        log.info("超时订单扫描完成，取消 {} 笔订单", orders.size());
    }

    private Long getUserIdOrThrow() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return userId;
    }
}




