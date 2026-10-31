package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.dto.OrderCreateDTO;
import com.gdufe.mall.vo.CreateOrderVO;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;

/** 前台订单（当前用户维度） */
public interface OrderService {

    CreateOrderVO create(OrderCreateDTO dto);

    PageResult<OrderItemVO> page(Integer status, Integer page, Integer size);

    OrderDetailVO detail(Long id);

    void pay(Long id);

    void cancel(Long id, String reason);

    void confirm(Long id);

    /** 超时取消，供定时任务调用（跨 Bean 调用，事务生效） */
    void timeoutCancel(Long id);
}
