package com.gdufe.mall.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体：{ code, msg, data }。
 * 约定：业务失败返回 HTTP 200 + code != 0；认证/权限失败由全局异常处理器返回 401/403。
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.msg = ResultCode.SUCCESS.getMsg();
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return fail(rc.getCode(), rc.getMsg());
    }
}
