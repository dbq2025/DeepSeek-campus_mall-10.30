package com.gdufe.mall.service.impl;

import com.gdufe.mall.entity.OrderItem;
import com.gdufe.mall.entity.Orders;
import com.gdufe.mall.vo.ActionVO;
import com.gdufe.mall.vo.OrderDetailVO;
import com.gdufe.mall.vo.OrderItemVO;
import com.gdufe.mall.vo.OrderProductVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单 VO 组装：statusText 与 actions 只由后端计算，前端只渲染不判断（契约 0.2）。
 * 用户动作与后台动作分开，避免前台看到「发货」这种越权按钮。
 */
final class OrderAssembler {

    private static final String[] STATUS_TEXT = {"待付款", "待发货", "待收货", "已完成", "已取消"};

    private OrderAssembler() {
    }

    static String statusText(Integer status) {
        if (status == null || status < 0 || status >= STATUS_TEXT.length) {
            return "未知";
        }
        return STATUS_TEXT[status];
    }

    static List<ActionVO> userActions(Integer status) {
        List<ActionVO> actions = new ArrayList<>();
        if (status != null && status == 0) {
            actions.add(new ActionVO("pay", "去支付", "primary"));
            actions.add(new ActionVO("cancel", "取消订单", "default"));
        } else if (status != null && status == 2) {
            actions.add(new ActionVO("confirm", "确认收货", "primary"));
        }
        return actions;
    }

    static List<ActionVO> adminActions(Integer status) {
        List<ActionVO> actions = new ArrayList<>();
        if (status != null && status == 1) {
            actions.add(new ActionVO("ship", "发货", "primary"));
        } else if (status != null && status == 2) {
            actions.add(new ActionVO("confirm", "确认收货", "primary"));
        }
        return actions;
    }

    static OrderItemVO toListItem(Orders o, List<OrderItem> items, boolean admin) {
        OrderItemVO vo = new OrderItemVO();
        fillBase(vo, o, items);
        vo.setActions(admin ? adminActions(o.getStatus()) : userActions(o.getStatus()));
        return vo;
    }

    static OrderDetailVO toDetail(Orders o, List<OrderItem> items, boolean admin) {
        OrderDetailVO vo = new OrderDetailVO();
        fillBase(vo, o, items);
        vo.setActions(admin ? adminActions(o.getStatus()) : userActions(o.getStatus()));
        vo.setTotalAmount(o.getTotalAmount());
        vo.setReceiverName(o.getReceiverName());
        vo.setReceiverPhone(o.getReceiverPhone());
        vo.setReceiverAddress(o.getReceiverAddress());
        vo.setRemark(o.getRemark());
        vo.setTrackingNo(o.getTrackingNo());
        vo.setPayTime(o.getPayTime());
        vo.setShipTime(o.getShipTime());
        vo.setFinishTime(o.getFinishTime());
        vo.setCancelTime(o.getCancelTime());
        vo.setCancelReason(o.getCancelReason());
        return vo;
    }

    private static void fillBase(OrderItemVO vo, Orders o, List<OrderItem> items) {
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setStatus(o.getStatus());
        vo.setStatusText(statusText(o.getStatus()));
        vo.setPayAmount(o.getPayAmount());
        vo.setCreateTime(o.getCreateTime());
        vo.setItems(toProducts(items));
    }

    static List<OrderProductVO> toProducts(List<OrderItem> items) {
        List<OrderProductVO> list = new ArrayList<>(items.size());
        for (OrderItem it : items) {
            OrderProductVO p = new OrderProductVO();
            p.setId(it.getId());
            p.setProductId(it.getProductId());
            p.setProductName(it.getProductName());
            p.setProductImage(it.getProductImage());
            p.setPrice(it.getPrice());
            p.setQuantity(it.getQuantity());
            p.setSpecText(it.getSpecText());
            p.setTotalAmount(it.getTotalAmount());
            list.add(p);
        }
        return list;
    }
}
