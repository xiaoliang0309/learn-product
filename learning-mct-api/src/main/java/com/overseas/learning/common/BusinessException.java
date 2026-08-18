package com.overseas.learning.common;

import lombok.Getter;

/**
 * 业务异常 — 在 Service 层抛出，由 GlobalExceptionHandler 统一处理
 *
 * 用法:
 *   throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
 *   throw new BusinessException(1001, "自定义错误消息");
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String msg;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}