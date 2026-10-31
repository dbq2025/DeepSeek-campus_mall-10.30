package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;

/** 后台订单管理 */
public interface AdminOrderService {

    PageResult<OrderItemVO> page(String orderNo, Integer status, Integer page, Integer size);

    OrderDetailVO detail(Long id);

    void ship(Long id, String trackingNo);

    void confirm(Long id);
}
