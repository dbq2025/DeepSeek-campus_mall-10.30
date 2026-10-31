package com.gdufe.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteDTO {

    @NotNull(message = "缺少商品 ID")
    private Long productId;
}
