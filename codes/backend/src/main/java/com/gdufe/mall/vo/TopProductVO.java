package com.gdufe.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 热销排行项 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductVO {
    private Long productId;
    private String productName;
    private Integer sales;
}
