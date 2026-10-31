package com.gdufe.mall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductForm {

    @NotNull(message = "请选择商品类型")
    private Long categoryId;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 100, message = "商品名称最长 100 个字符")
    private String name;

    @Size(max = 200, message = "副标题最长 200 个字符")
    private String subtitle;

    @Size(max = 255, message = "图片路径最长 255 个字符")
    private String mainImage;

    @NotNull(message = "请输入售价")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 999999, message = "库存不能超过 999999")
    private Integer stock;

    /** 规格定义 JSON，空表示无规格 */
    private String specJson;

    private String detail;

    @NotNull(message = "请选择上下架状态")
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;
}
