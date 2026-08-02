package com.flashmall.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {

    /**
     * 当前页
     */
    private Long page = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;

    /**
     * 商品名称（模糊查询）
     */
    private String name;

    /**
     * 商品状态
     */
    private Integer status;

}