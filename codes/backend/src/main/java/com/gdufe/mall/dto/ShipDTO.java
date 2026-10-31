package com.gdufe.mall.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShipDTO {

    /** 可空；为空时只改状态不写单号 */
    @Size(max = 50, message = "运单号最长 50 个字符")
    private String trackingNo;
}
