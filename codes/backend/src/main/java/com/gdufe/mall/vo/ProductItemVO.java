package com.gdufe.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 商品列表项（前台） */
@Data
public class ProductItemVO {
    private Long id;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private Long categoryId;
    private String categoryName;
}
