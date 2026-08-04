package com.flashmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;


@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private Long orderId;


    private Long productId;


    /**
     * 商品快照
     */
    private String productName;


    private BigDecimal price;


    private Integer quantity;

}