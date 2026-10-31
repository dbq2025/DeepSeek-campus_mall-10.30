package com.gdufe.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryForm {

    @NotBlank(message = "请输入类型名称")
    @Size(max = 50, message = "类型名称最长 50 个字符")
    private String name;

    /** 不传按 0 处理 */
    private Integer sort;
}
