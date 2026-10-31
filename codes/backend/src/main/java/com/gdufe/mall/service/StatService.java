package com.gdufe.mall.service;

import com.gdufe.mall.vo.OrderStatusCountVO;
import com.gdufe.mall.vo.SalesTrendVO;
import com.gdufe.mall.vo.StatsOverviewVO;
import com.gdufe.mall.vo.TopProductVO;

import java.util.List;

public interface StatService {

    StatsOverviewVO overview();

    SalesTrendVO sales(int days);

    List<OrderStatusCountVO> orderStatus();

    List<TopProductVO> topProducts(int limit);
}
