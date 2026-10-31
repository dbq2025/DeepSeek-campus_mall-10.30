package com.gdufe.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    /** true 购物车下单（用 cartIds）；false 立即购买（用 items） */
    @NotNull(message = "缺少下单来源标识")
    private Boolean fromCart;

    private List<Long> cartIds;

    private List<OrderItemInputDTO> items;

    @NotBlank(message = "请填写收货人")
    @Size(max = 50, message = "收货人最长 50 个字符")
    private String receiverName;

    @NotBlank(message = "请填写手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @NotBlank(message = "请填写收货地址")
    @Size(max = 255, message = "地址最长 255 个字符")
    private String receiverAddress;

    @Size(max = 200, message = "备注最长 200 个字符")
    private String remark;
}
