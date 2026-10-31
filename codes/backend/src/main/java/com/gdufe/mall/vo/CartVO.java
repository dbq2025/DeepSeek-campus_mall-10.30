package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 购物车聚合视图 */
@Data
public class CartVO {
    private List<CartItemVO> items = new ArrayList<>();
    private Integer totalQuantity = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
