package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 订单列表项（前台「我的订单」/ 后台列表共用） */
@Data
public class OrderItemVO {
    private Long id;
    private String orderNo;
    private Integer status;
    private String statusText;
    private BigDecimal payAmount;
    private LocalDateTime createTime;
    private List<OrderProductVO> items = new ArrayList<>();
    private List<ActionVO> actions = new ArrayList<>();
    /** 后台列表需要，前台为 null */
    private String userName;
}
