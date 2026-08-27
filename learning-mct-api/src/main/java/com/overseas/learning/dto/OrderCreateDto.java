package com.overseas.learning.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 下单 DTO —— 接收前端下单参数
 *
 * 【对齐 qingo】
 *   Controller 不直接拼实体，而是用 DTO 接收 @RequestBody 的 JSON，
 *   再在 Biz 层把 DTO 转成实体（DTO → Entity）。
 *
 * @NotNull = 该字段不能为 null（配合 @Valid 触发校验）
 */
@Data
public class OrderCreateDto {

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    private Long shopId;

    @NotNull(message = "金额不能为空")
    private Long amount;         // 单位：分

    private Integer receiveType; // 1-自收 2-代收

    private String paymentMethod; // WECHAT/ALIPAY/STRIPE
}
