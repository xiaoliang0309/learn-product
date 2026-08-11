package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 订单实体 — 对应 trade_order 表
 *
 * 注解: @Data + @Builder + @NoArgsConstructor + @AllArgsConstructor（对标 qingo）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrder {
    private Long id;
    private String orderNo;
    private Long merchantId;
    private Long shopId;

    /** 交易金额（分），例如 599 = 5.99 USD */
    private Long amount;

    private String currency;
    private Integer receiveType;
    private Integer status;
    private String paymentMethod;
    private Date createdAt;
}