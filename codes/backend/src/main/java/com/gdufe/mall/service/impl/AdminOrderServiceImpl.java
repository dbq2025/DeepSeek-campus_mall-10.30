package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.entity.OrderItem;
import com.gdufe.mall.entity.Orders;
import com.gdufe.mall.entity.User;
import com.gdufe.mall.mapper.OrderItemMapper;
import com.gdufe.mall.mapper.OrdersMapper;
import com.gdufe.mall.mapper.UserMapper;
import com.gdufe.mall.service.AdminOrderService;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<OrderItemVO> page(String orderNo, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            w.like(Orders::getOrderNo, orderNo.trim());
        }
        if (status != null) {
            w.eq(Orders::getStatus, status);
        }
        w.orderByDesc(Orders::getCreateTime).orderByDesc(Orders::getId);

        Page<Orders> result = ordersMapper.selectPage(PageUtil.of(page, size), w);
        List<Long> orderIds = result.getRecords().stream().map(Orders::getId).toList();
        Map<Long, List<OrderItem>> itemMap = loadItems(orderIds);
        Map<Long, String> userNames = loadUserNames(
                result.getRecords().stream().map(Orders::getUserId).toList());

        List<OrderItemVO> records = new ArrayList<>(result.getRecords().size());
        for (Orders o : result.getRecords()) {
            OrderItemVO vo = OrderAssembler.toListItem(o,
                    itemMap.getOrDefault(o.getId(), Collections.emptyList()), true);
            vo.setUserName(userNames.get(o.getUserId()));
            records.add(vo);
        }
        PageResult<OrderItemVO> pr = PageResult.of(result);
        pr.setRecords(records);
        return pr;
    }

    @Override
    public OrderDetailVO detail(Long id) {
        Orders o = ordersMapper.selectById(id);
        if (o == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        Map<Long, List<OrderItem>> itemMap = loadItems(List.of(id));
        OrderDetailVO vo = OrderAssembler.toDetail(o,
                itemMap.getOrDefault(id, Collections.emptyList()), true);
        vo.setUserName(loadUserNames(List.of(o.getUserId())).get(o.getUserId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id, String trackingNo) {
        String tracking = StringUtils.hasText(trackingNo) ? trackingNo.trim() : null;
        int rows = ordersMapper.ship(id, tracking);
        if (rows == 0) {
            Orders o = ordersMapper.selectById(id);
            if (o == null) {
                throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
            }
            if (o.getStatus() != null && o.getStatus() == 2) {
                // 重复发货：幂等成功，且不覆盖原运单号（D4）
                return;
            }
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "只有待发货订单可以发货");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        int rows = ordersMapper.confirmByAdmin(id);
        if (rows == 0) {
            Orders o = ordersMapper.selectById(id);
            if (o == null) {
                throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
            }
            if (o.getStatus() != null && o.getStatus() == 3) {
                return;
            }
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "只有待收货订单可以确认收货");
        }
    }

    private Map<Long, List<OrderItem>> loadItems(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds)
                .orderByAsc(OrderItem::getId));
        Map<Long, List<OrderItem>> map = new HashMap<>();
        for (OrderItem it : items) {
            map.computeIfAbsent(it.getOrderId(), k -> new ArrayList<>()).add(it);
        }
        return map;
    }

    private Map<Long, String> loadUserNames(List<Long> userIds) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = userIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return map;
        }
        for (User u : userMapper.selectBatchIds(ids)) {
            map.put(u.getId(), u.getUsername());
        }
        return map;
    }
}
