package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 订单中的商品行快照 */
@Data
public class OrderProductVO {
    /** 订单明细 ID，前端列表 key（契约 v1.1 补齐） */
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private String specText;
    private BigDecimal totalAmount;
}
