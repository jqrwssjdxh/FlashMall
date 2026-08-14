package com.flashmall.mq;

import com.flashmall.dto.OrderMessageDTO;
import com.flashmall.service.ProductService;
import com.flashmall.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单消息发送确认跟踪器
 * <p>
 * 职责：记录已扣库存但尚未确认的消息。
 * orderNo -> 消息（含 productId/quantity），确认成功后移除；
 * 失败时通过 remove(orderNo) 补偿库存。
 * ConcurrentHashMap.remove 是原子的，保证同一 orderNo 的补偿只执行一次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageTracker {

    /** 补偿类型：决定补偿边界 */
    public enum CompensateType {
        /** 同步发送异常：submitOrder 事务将回滚，MySQL 自动恢复，只恢复 Redis */
        SYNC_SEND_FAILURE,
        /** Confirm NACK：submitOrder 事务已提交，Redis + MySQL 都需恢复 */
        CONFIRM_NACK,
        /** 路由失败（returns）：submitOrder 事务已提交，Redis + MySQL 都需恢复 */
        RETURNED_MESSAGE
    }

    private final StockService stockService;
    private final ProductService productService;

    private final Map<String, OrderMessageDTO> pendingMessages = new ConcurrentHashMap<>();

    /** 发送前登记 */
    public void register(OrderMessageDTO message) {
        pendingMessages.put(message.getOrderNo(), message);
    }

    /** 消息确认到达 Exchange：移除待确认记录 */
    public void onAck(String orderNo) {
        pendingMessages.remove(orderNo);
        log.info("[OrderMessageTracker] 消息确认成功 orderNo={}", orderNo);
    }

    /**
     * 补偿库存（仅执行一次）
     *
     * @param type 补偿类型：SYNC_SEND_FAILURE 只恢复 Redis（MySQL 由事务回滚恢复）；
     *             CONFIRM_NACK / RETURNED_MESSAGE 同时恢复 Redis + MySQL（独立事务）
     */
    public void compensate(String orderNo, CompensateType type, String reason) {
        OrderMessageDTO message = pendingMessages.remove(orderNo);
        if (message == null) {
            // 已补偿过或从未登记，直接忽略
            log.warn("[OrderMessageTracker] 补偿跳过（已处理或未登记）orderNo={}, type={}, reason={}",
                    orderNo, type, reason);
            return;
        }

        // Redis 库存恢复（任何路径都需要）
        stockService.restoreStock(message.getProductId(), message.getQuantity());

        if (type != CompensateType.SYNC_SEND_FAILURE) {
            // 异步失败路径：submitOrder 事务已提交，MySQL 需独立事务（REQUIRES_NEW）恢复
            productService.restoreStock(message.getProductId(), message.getQuantity());
        } else {
            // 同步失败路径：submitOrder 事务会 rollback，MySQL 由数据库自动恢复，禁止主动补偿
            log.info("[OrderMessageTracker] 同步失败路径：MySQL 由事务回滚自动恢复 productId={}",
                    message.getProductId());
        }

        log.warn("[OrderMessageTracker] 库存已补偿 productId={}, quantity={}, orderNo={}, type={}, reason={}",
                message.getProductId(), message.getQuantity(), orderNo, type, reason);
    }
}
