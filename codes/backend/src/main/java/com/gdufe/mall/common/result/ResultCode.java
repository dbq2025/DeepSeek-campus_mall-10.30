package com.gdufe.mall.common.result;

import lombok.Getter;

/**
 * 全局错误码。只增不改：已上线的码不允许改变含义。
 * 分段：1xxx 参数 / 2xxx 认证授权 / 3xxx 业务 / 5xxx 系统。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "ok"),

    PARAM_ERROR(1001, "参数校验失败"),
    BODY_ERROR(1002, "请求体格式错误"),

    UNAUTHORIZED(2001, "未登录或缺少令牌"),
    TOKEN_INVALID(2002, "令牌无效或已过期"),
    FORBIDDEN(2003, "无权限"),
    LOGIN_FAILED(2004, "账号或密码错误"),

    STOCK_NOT_ENOUGH(3001, "库存不足"),
    STATUS_NOT_ALLOWED(3002, "当前状态不允许该操作"),
    NOT_FOUND(3003, "资源不存在"),
    DUPLICATE_OPERATION(3004, "请勿重复操作"),
    PRODUCT_OFF_SHELF(3005, "商品已下架"),

    SYSTEM_ERROR(5000, "系统异常");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
