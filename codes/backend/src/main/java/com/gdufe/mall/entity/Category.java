package com.gdufe.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 商品类型，schema.sql: category */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    /** 越小越前 */
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
