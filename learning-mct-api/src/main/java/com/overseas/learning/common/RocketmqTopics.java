package com.overseas.learning.common;

/**
 * RocketMQ Topic 统一管理（对齐 qingo 命名：topic-模块-业务）
 *
 * 【为什么单独一份，不和 KafkaTopics 混？】
 *   qingo 里 Kafka 和 RocketMQ 是两套独立集群，topic 也是两套，
 *   就像 qingo 的真实 topic：
 *     Kafka    : topic-mct-data-export            （商户数据导出，量大）
 *     RocketMQ : topic-order-divide-complete-mp   （分账完成，要可靠投递+重试）
 *     RocketMQ : topic-order-delay-cancel-mp      （延迟关单，要延迟消息）
 */
public class RocketmqTopics {

    /** 分账完成通知：对应 qingo 的 topic-order-divide-complete-mp（异步+失败重试场景） */
    public static final String ORDER_DIVIDE_COMPLETE = "topic-order-divide-complete-mp";

    private RocketmqTopics() {
    }
}
