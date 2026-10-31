package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 后台概览 5 项指标（计数用 Integer，避免被全局 Long->String 转成字符串） */
@Data
public class StatsOverviewVO {
    private Integer todayOrderCount;
    private BigDecimal todaySalesAmount;
    private Integer pendingShipmentCount;
    private Integer totalProductCount;
    private Integer totalUserCount;
}
