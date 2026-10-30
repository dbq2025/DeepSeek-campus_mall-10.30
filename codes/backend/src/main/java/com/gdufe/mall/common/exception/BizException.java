package com.gdufe.mall.common.exception;

import com.gdufe.mall.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常。所有可预期的失败都抛它，不要 return null 表示失败。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(ResultCode rc) {
        super(rc.getMsg());
        this.code = rc.getCode();
    }

    public BizException(ResultCode rc, String msg) {
        super(msg);
        this.code = rc.getCode();
    }
}
