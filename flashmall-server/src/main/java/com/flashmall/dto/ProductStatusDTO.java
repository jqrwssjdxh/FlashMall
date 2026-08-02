package com.flashmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;

}