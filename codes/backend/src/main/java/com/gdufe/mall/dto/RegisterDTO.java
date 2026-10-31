package com.gdufe.mall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = "^\\S{4,20}$", message = "用户名需 4-20 位且不含空格")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度需 6-20 位")
    private String password;

    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    /** 可空；空串必须转 NULL，否则唯一索引会冲突 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 个字符")
    private String email;
}
