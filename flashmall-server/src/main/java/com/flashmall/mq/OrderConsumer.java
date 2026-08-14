package com.flashmall.mq;

import com.flashmall.config.RabbitConfig;
import com.flashmall.dto.OrderMessageDTO;
import com.flashmall.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE, ackMode = "MANUAL")
    public void onOrderMessage(OrderMessageDTO message,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 异步创建订单（只落库，不扣库存——库存已在 submit 阶段扣过）
            orderService.createOrderByMessage(message);
            channel.basicAck(deliveryTag, false);
            log.info("[OrderConsumer] 订单创建成功并确认 orderNo={}", message.getOrderNo());
        } catch (Exception e) {
            log.error("[OrderConsumer] 订单创建失败 orderNo={}, 错误: {}", message.getOrderNo(), e.getMessage());
            try {
                // 消费失败：重回队列（或进死信队列，当前先 requeue 重试）
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("[OrderConsumer] basicNack 失败", ex);
            }
        }
    }
}
