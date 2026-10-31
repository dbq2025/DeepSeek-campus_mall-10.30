package com.gdufe.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户表（含管理员），schema.sql: user */
@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    /** BCrypt 密文，禁止明文落库 */
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    /** USER / ADMIN */
    private String role;
    /** 1 正常 0 禁用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
