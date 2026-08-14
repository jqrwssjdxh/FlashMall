package com.flashmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flashmall.common.UserContext;
import com.flashmall.common.lock.DistributedLockService;
import com.flashmall.constant.OrderStatus;
import com.flashmall.constant.ResultCode;
import com.flashmall.dto.OrderMessageDTO;
import com.flashmall.entity.Order;
import com.flashmall.entity.OrderItem;
import com.flashmall.entity.Product;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.OrderItemMapper;
import com.flashmall.mapper.OrderMapper;
import com.flashmall.mapper.ProductMapper;
import com.flashmall.mq.OrderMessageTracker;
import com.flashmall.mq.OrderProducer;
import com.flashmall.service.OrderService;
import com.flashmall.service.StockService;
import com.flashmall.vo.OrderDetailVO;
import com.flashmall.vo.OrderItemVO;
import com.flashmall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;
    private final OrderProducer orderProducer;
    private final OrderMessageTracker messageTracker;
    private final DistributedLockService distributedLockService;

    /** 防重复提交锁等待时间：0 = 非阻塞，拿不到锁立即失败 */
    private static final long LOCK_WAIT_TIME = 0;
    /** 锁自动释放时间：业务毫秒级完成，10 秒足够 */
    private static final long LOCK_LEASE_TIME = 10;

    @Override
    @Transactional
    public String submitOrder(Long productId, Integer quantity) {
        Long userId = getUserIdOrThrow();

        // 防重复提交：同一用户 + 同一商品 只允许一个请求进入核心流程
        String lockKey = "lock:order:create:" + userId + ":" + productId;
        boolean locked = distributedLockService.tryLock(lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
        if (!locked) {
            log.warn("[OrderService] 重复提交拦截 userId={}, productId={}", userId, productId);
            throw new BusinessException(ResultCode.REPEAT_SUBMIT);
        }

        try {
            return doSubmitOrder(userId, productId, quantity);
        } finally {
            // 无论正常结束还是异常（Redis/MySQL/MQ），都释放锁
            distributedLockService.unlock(lockKey);
        }
    }

    /** 原 submitOrder 核心流程（锁保护区内执行） */
    private String doSubmitOrder(Long userId, Long productId, Integer quantity) {
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

        // 3. 生成订单号，发送 RabbitMQ 消息（异步落库）
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        OrderMessageDTO message = new OrderMessageDTO(userId, productId, quantity, orderNo);
        try {
            orderProducer.sendOrderMessage(message);
        } catch (AmqpException e) {
            // 同步发送失败：
            // 1. 只恢复 Redis（Tracker 按 SYNC_SEND_FAILURE 处理）
            // 2. MySQL 由本事务 rollback 自动恢复，禁止在此主动补偿（否则与挂起事务争锁）
            // 3. 抛业务异常，事务回滚，HTTP 返回明确错误
            messageTracker.compensate(orderNo, OrderMessageTracker.CompensateType.SYNC_SEND_FAILURE,
                    "send exception: " + e.getMessage());
            log.error("[OrderService] 订单消息发送失败，Redis 库存已恢复，MySQL 由事务回滚 orderNo={}", orderNo, e);
            throw new BusinessException(ResultCode.ORDER_CREATE_FAIL);
        }
        log.info("[OrderService] 订单消息已发送 orderNo={}", orderNo);

        return orderNo;
    }

    @Override
    @Transactional
    public Order createOrderByMessage(OrderMessageDTO message) {
        // 幂等检查：订单已存在则直接返回（重复消费场景）
        Order existing = getOrderByOrderNo(message.getOrderNo());
        if (existing != null) {
            log.info("[OrderService] 订单已存在，跳过创建 orderNo={}", message.getOrderNo());
            return existing;
        }

        Product product = productMapper.selectById(message.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        try {
            // 创建订单
            Order order = new Order();
            order.setUserId(message.getUserId());
            order.setOrderNo(message.getOrderNo());
            order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(message.getQuantity())));
            order.setStatus(OrderStatus.WAIT_PAY.getCode());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(order);

            // 创建订单明细
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(message.getProductId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(message.getQuantity());
            orderItemMapper.insert(item);

            log.info("[OrderService] 订单落库成功 orderNo={}", message.getOrderNo());
            return order;
        } catch (DuplicateKeyException e) {
            // 并发重复消费：唯一索引兜底，视为幂等成功
            log.warn("[OrderService] 并发重复消费，唯一索引拦截 orderNo={}", message.getOrderNo());
            return getOrderByOrderNo(message.getOrderNo());
        }
    }

    /** 按 orderNo 查订单（幂等检查用） */
    private Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo)
        );
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




