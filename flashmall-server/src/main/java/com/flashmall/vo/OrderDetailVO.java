package com.flashmall.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flashmall.constant.OrderStatus;
import com.flashmall.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class OrderDetailVO {

    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;

    private Integer status;

    private String statusName;  // 自动填充，无需手动 set

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private List<OrderItemVO> items = new ArrayList<>(); // 避免 NPE

    // ===== 工厂方法：封装组装逻辑 =====
    public static OrderDetailVO from(Order order, List<OrderItemVO> items) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setCreateTime(order.getCreateTime());

        vo.setStatus(order.getStatus());
        // 直接利用 fromCode 拿到枚举对象，再拿中文描述
        vo.setStatusName(OrderStatus.fromCode(order.getStatus()).getDesc());

        // 防御性赋值
        vo.setItems(items != null ? items : Collections.emptyList());

        return vo;
    }
}