package com.gdufe.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddDTO {

    @NotNull(message = "缺少商品 ID")
    private Long productId;

    @NotNull(message = "请选择购买数量")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    /** 有规格商品必填，格式 名称:值;名称:值 */
    private String specText;
}
