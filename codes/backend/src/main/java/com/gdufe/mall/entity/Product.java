package com.gdufe.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品（不做真 SKU），schema.sql: product */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    /** 单维库存，下单即扣 */
    private Integer stock;
    /** 规格定义 JSON，空表示无规格 */
    private String specJson;
    private String detail;
    /** 1 上架 0 下架 */
    private Integer status;
    /** 累计销量，支付成功时累加 */
    private Integer sales;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
