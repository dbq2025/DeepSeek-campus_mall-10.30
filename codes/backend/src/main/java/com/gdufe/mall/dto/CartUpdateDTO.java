package com.gdufe.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartUpdateDTO {

    @NotNull(message = "请输入数量")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;
}
