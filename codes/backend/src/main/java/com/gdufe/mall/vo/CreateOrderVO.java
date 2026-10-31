package com.gdufe.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 下单返回：前端拿 orderId 跳详情页 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderVO {
    private Long orderId;
    private String orderNo;
    private BigDecimal payAmount;
}
