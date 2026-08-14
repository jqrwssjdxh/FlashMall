package com.flashmall.config;

import com.flashmall.mq.OrderMessageTracker;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "flashmall.order.exchange";
    public static final String ORDER_QUEUE = "flashmall.order.queue";
    public static final String ORDER_ROUTING_KEY = "flashmall.order.create";

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         OrderMessageTracker messageTracker) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());

        // 生产者确认（异步回调）
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String orderNo = correlationData != null ? correlationData.getId() : null;
            if (ack) {
                // 确认成功：只记录日志，移除待确认记录
                messageTracker.onAck(orderNo);
            } else {
                // 确认失败（NACK）：事务已提交，Redis + MySQL 都需补偿
                messageTracker.compensate(orderNo,
                        OrderMessageTracker.CompensateType.CONFIRM_NACK,
                        "confirm NACK: " + cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            // mandatory=true 时消息无法路由到队列：事务已提交，Redis + MySQL 都需补偿
            String orderNo = returned.getMessage().getMessageProperties()
                    .getCorrelationId();
            messageTracker.compensate(orderNo,
                    OrderMessageTracker.CompensateType.RETURNED_MESSAGE,
                    "message routed failed: " + returned.getReplyText());
        });
        return rabbitTemplate;
    }
}
