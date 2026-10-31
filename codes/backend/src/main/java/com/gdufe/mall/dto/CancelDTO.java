package com.gdufe.mall.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelDTO {

    @Size(max = 50, message = "取消原因最长 50 个字符")
    private String reason;
}
