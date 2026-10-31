package com.gdufe.mall.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 后台商品列表项：列表字段 + 上下架状态 + 创建时间 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends ProductItemVO {
    /** 1 上架 0 下架 */
    private Integer status;
    private LocalDateTime createTime;
}
