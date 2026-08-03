package com.flashmall.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmall.common.UserContext;
import com.flashmall.constant.ResultCode;
import com.flashmall.entity.Order;
import com.flashmall.entity.OrderItem;
import com.flashmall.entity.Product;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.OrderItemMapper;
import com.flashmall.mapper.OrderMapper;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.service.OrderService;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderItemVO;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final ProductMapper productMapper;



    @Override
    @Transactional
    public Order createOrder(Long productId, Integer quantity) {


        // 1. 获取当前用户
        Long userId = UserContext.getUserId();



        // 2. 查询商品
        Product product = productMapper.selectById(productId);


        if(product == null){

            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);

        }



        // 3. 判断库存

        if(product.getStock() < quantity){

            throw new BusinessException(ResultCode.OUT_OF_STOCK);

        }



        // 4. 扣减库存

        product.setStock(
                product.getStock() - quantity
        );


        productMapper.updateById(product);



        // 5. 创建订单

        Order order = new Order();

        order.setUserId(userId);

        order.setOrderNo(
                UUID.randomUUID()
                        .toString()
                        .replace("-","")
        );


        BigDecimal total =
                product.getPrice()
                        .multiply(
                                BigDecimal.valueOf(quantity)
                        );


        order.setTotalAmount(total);


        // 待支付

        order.setStatus(0);


        order.setCreateTime(LocalDateTime.now());

        order.setUpdateTime(LocalDateTime.now());



        orderMapper.insert(order);



        // 6. 创建订单明细


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


        Long userId = UserContext.getUserId();


        List<Order> orders =
                orderMapper.selectList(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getUserId,userId)
                                .orderByDesc(Order::getCreateTime)
                );



        return orders.stream()
                .map(order -> {


                    OrderVO vo = new OrderVO();

                    vo.setId(order.getId());

                    vo.setOrderNo(order.getOrderNo());

                    vo.setTotalAmount(order.getTotalAmount());

                    vo.setStatus(order.getStatus());

                    vo.setCreateTime(order.getCreateTime());


                    return vo;

                })
                .collect(Collectors.toList());

    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {


        Long userId = UserContext.getUserId();


        // 1. 查询订单，并校验用户
        Order order =
                orderMapper.selectOne(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getId, orderId)
                                .eq(Order::getUserId, userId)
                );


        if(order == null){

            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);

        }



        // 2. 查询订单商品

        List<OrderItem> items =
                orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>()
                                .eq(OrderItem::getOrderId, orderId)
                );



        // 3. 组装VO

        OrderDetailVO vo = new OrderDetailVO();


        vo.setId(order.getId());

        vo.setOrderNo(order.getOrderNo());

        vo.setTotalAmount(order.getTotalAmount());

        vo.setStatus(order.getStatus());

        vo.setCreateTime(order.getCreateTime());



        List<OrderItemVO> itemVOList =
                items.stream()
                        .map(item -> {

                            OrderItemVO itemVO =
                                    new OrderItemVO();


                            itemVO.setProductId(
                                    item.getProductId()
                            );


                            itemVO.setProductName(
                                    item.getProductName()
                            );


                            itemVO.setPrice(
                                    item.getPrice()
                            );


                            itemVO.setQuantity(
                                    item.getQuantity()
                            );


                            return itemVO;

                        })
                        .toList();



        vo.setItems(itemVOList);


        return vo;

    }


}