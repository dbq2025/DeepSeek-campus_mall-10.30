package com.gdufe.mall.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 商品详情：列表字段 + 规格/详情/上架时间/收藏态 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailVO extends ProductItemVO {
    /** 1 上架 0 下架；后台编辑回填需要，前台详情恒为 1 */
    private Integer status;
    /** 规格定义 JSON 字符串，null 表示无规格 */
    private String specJson;
    private String detail;
    private LocalDateTime createTime;
    private Boolean favorited;
}
