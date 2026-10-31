package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.service.StatService;
import com.gdufe.mall.vo.OrderStatusCountVO;
import com.gdufe.mall.vo.SalesTrendVO;
import com.gdufe.mall.vo.StatsOverviewVO;
import com.gdufe.mall.vo.TopProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatService statService;

    /** STAT-01 概览指标 */
    @GetMapping("/overview")
    public Result<StatsOverviewVO> overview() {
        return Result.ok(statService.overview());
    }

    /** STAT-02 销售趋势（days=7|30） */
    @GetMapping("/sales")
    public Result<SalesTrendVO> sales(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(statService.sales(days));
    }

    /** STAT-03 订单状态分布（恒返回 5 项） */
    @GetMapping("/order-status")
    public Result<List<OrderStatusCountVO>> orderStatus() {
        return Result.ok(statService.orderStatus());
    }

    /** STAT-04 热销排行 */
    @GetMapping("/top-products")
    public Result<List<TopProductVO>> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(statService.topProducts(limit));
    }
}
