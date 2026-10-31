package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 购物车行 */
@Data
public class CartItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private String mainImage;
    /** 当前售价（不是加入时价格，下单以前端展示+后端重算为准） */
    private BigDecimal price;
    private Integer quantity;
    private String specText;
    private Integer stock;
    /** 1 上架 0 下架，前端据此禁用结算 */
    private Integer productStatus;
}
