package com.gdufe.mall.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单详情：列表字段 + 收货信息 + 时间轴 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderItemVO {
    private BigDecimal totalAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String trackingNo;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
}
