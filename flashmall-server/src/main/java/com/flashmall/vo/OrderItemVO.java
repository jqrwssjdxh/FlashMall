package com.flashmall.vo;


import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderItemVO {


    private Long productId;


    private String productName;


    private BigDecimal price;


    private Integer quantity;


}