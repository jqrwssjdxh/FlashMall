package com.flashmall.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@TableName("cart")
public class Cart {


    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private Long userId;


    private Long productId;


    private String productName;


    private BigDecimal price;


    private Integer quantity;


    private LocalDateTime createTime;


    private LocalDateTime updateTime;

}