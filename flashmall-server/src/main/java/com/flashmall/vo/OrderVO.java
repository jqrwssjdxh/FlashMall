package com.flashmall.vo;
import com.flashmall.constant.OrderStatus;
import com.flashmall.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;

    public static OrderVO from(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusName(OrderStatus.fromCode(order.getStatus()).getDesc());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }
}
