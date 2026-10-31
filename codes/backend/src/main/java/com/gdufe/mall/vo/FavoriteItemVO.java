package com.gdufe.mall.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 收藏列表项：收藏信息 + 商品字段 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteItemVO extends ProductItemVO {
    private Long favoriteId;
    private LocalDateTime favoriteTime;
}
