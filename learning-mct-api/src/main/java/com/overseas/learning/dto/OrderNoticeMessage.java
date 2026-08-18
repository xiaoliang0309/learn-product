package com.overseas.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单通知消息体 —— 通过 Kafka 传递的内容
 *
 * 【说明】
 *   Kafka 里传的是字符串，所以这个对象会被序列化成 JSON 发送，
 *   消费者收到后再反序列化回这个对象（qingo 用 JSON.toJSONString / JSON.parseObject）。
 *
 *   消息体只放「消费者需要的字段」，不要把整个订单实体塞进来——
 *   传得越少，生产者和消费者的耦合越低。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNoticeMessage {

    /** 订单号 */
    private String orderNo;

    /** 商户ID */
    private Long merchantId;

    /** 商户名称（消费者发通知时直接展示，不用再去查库） */
    private String merchantName;

    /** 下单时间（格式化好的字符串） */
    private String createTime;
}
