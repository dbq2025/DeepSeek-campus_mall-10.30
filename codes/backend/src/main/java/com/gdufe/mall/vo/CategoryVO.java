package com.gdufe.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 后台类型列表项（带引用商品数，决定删除按钮是否可用） */
@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sort;
    private Integer productCount;
    private LocalDateTime createTime;
}
