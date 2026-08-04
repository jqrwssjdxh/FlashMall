package com.flashmall.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CartAddDTO {


    @NotNull(message = "商品id不能为空")
    private Long productId;


    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

}