package com.overseas.learning.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 *
 * 规范:
 *   0xxx → 通用
 *   1xxx → 商户模块
 *   2xxx → 进件模块
 *   3xxx → 订单模块
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ===== 通用（0xxx） =====
    SUCCESS(0, "success"),
    SYSTEM_ERROR(-1, "系统异常"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1002, "数据不存在"),

    // ===== 商户模块（1xxx） =====
    MERCHANT_EMAIL_EXISTS(1001, "邮箱已存在"),
    MERCHANT_NAME_EXISTS(1002, "商户名称已存在"),
    MERCHANT_NOT_FOUND(1003, "商户不存在"),

    // ===== 进件模块（2xxx） =====
    ONBOARDING_NOT_FOUND(2001, "进件记录不存在"),
    ONBOARDING_ALREADY_APPROVED(2002, "进件已审核通过，无法修改"),
    ONBOARDING_PAY_CENTER_FAIL(2003, "调用支付中台失败"),

    // ===== 订单模块（3xxx） =====
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ;

    private final int code;
    private final String msg;
}