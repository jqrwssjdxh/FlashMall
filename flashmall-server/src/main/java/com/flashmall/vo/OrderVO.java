package com.flashmall.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class OrderVO {


    private Long id;


    private String orderNo;


    private BigDecimal totalAmount;


    /**
     * 订单状态
     *
     * 0 待支付
     * 1 已支付
     * 2 已取消
     */
    private Integer status;


    private LocalDateTime createTime;


}