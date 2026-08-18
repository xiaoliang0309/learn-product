package com.overseas.learning.common;

/**
 * Kafka Topic 统一管理
 *
 * 【命名规范】对齐 qingo：topic-模块-业务
 *   qingo 真实例子：
 *     topic-mct-data-export              → 商户数据导出
 *     topic-device-network-update-handle → 设备网络状态上报
 *     topic-firm-data-export             → 企业数据导出
 *
 * 【为什么集中定义？】和 RedisKeys 一个道理：
 *   生产者和消费者要用「同一个字符串」才能对上，集中定义避免两边写错。
 */
public class KafkaTopics {

    /** 订单通知：下单后发消息到这个 topic，消费者异步处理（发通知/记账等） */
    public static final String ORDER_NOTICE = "topic-order-notice";

    private KafkaTopics() {
    }
}
