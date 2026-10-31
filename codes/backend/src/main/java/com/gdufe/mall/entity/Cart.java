package com.gdufe.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 购物车行，schema.sql: cart（唯一键 user_id + product_id + spec_text） */
@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long productId;
    private Integer quantity;
    /** 规格快照，无规格存空串（不能存 null，唯一键包含该列） */
    private String specText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
