package com.flashmall.mq;

import com.flashmall.config.RabbitConfig;
import com.flashmall.dto.OrderMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;
    private final OrderMessageTracker messageTracker;

    public void sendOrderMessage(OrderMessageDTO message) {
        // 发送前登记：用于 confirm NACK / 发送异常时补偿库存
        messageTracker.register(message);

        // correlationId 直接复用 orderNo，confirm 回调能定位到具体消息
        CorrelationData correlationData = new CorrelationData(message.getOrderNo());

        rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_ROUTING_KEY,
                message,
                correlationData
        );
        log.info("[OrderProducer] 订单消息已发送 orderNo={}", message.getOrderNo());
    }
}
