package com.overseas.learning.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 进件提交 DTO — 简化版
 */
@Data
public class OnboardingSubmitDto {

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @NotNull(message = "收款类型不能为空")
    private Integer receiveType;  // 1-自收 2-代收

    @NotNull(message = "支付类型不能为空")
    private Integer paymentType;  // 3-PAX 4-Wizar 5-Qingo

    /** 进件表单数据（JSON 字符串） */
    private String formData;
}