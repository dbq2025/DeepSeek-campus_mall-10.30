package com.gdufe.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemInputDTO {

    @NotNull(message = "缺少商品 ID")
    private Long productId;

    @NotNull(message = "请选择购买数量")
    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 999, message = "单件商品最多购买 999 件")
    private Integer quantity;

    private String specText;
}
