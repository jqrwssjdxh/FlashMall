package com.flashmall.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@TableName("orders")
public class Order {

    private Long id;

    private Long userId;

    private String orderNo;

    private BigDecimal totalAmount;

    /**
     * 订单状态
     * 0 待支付
     * 1 已支付
     * 2 已取消
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}