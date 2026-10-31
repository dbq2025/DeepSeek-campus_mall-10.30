package com.gdufe.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 订单状态分布，5 个状态恒返回（无单也返回 0） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCountVO {
    private Integer status;
    private String statusText;
    private Integer count;
}
