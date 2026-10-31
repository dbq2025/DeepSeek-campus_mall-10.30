package com.gdufe.mall.vo;

import lombok.Data;

/** 用户信息（不含密码等敏感字段） */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
}
