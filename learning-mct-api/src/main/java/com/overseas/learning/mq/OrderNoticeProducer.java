package com.overseas.learning.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.common.KafkaTopics;
import com.overseas.learning.dto.OrderNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单通知「生产者」—— 负责把消息发到 Kafka
 *
 * 【对齐 qingo】
 *   qingo 的发送写法（BizExportServiceImpl）：
 *     kafkaTemplate.send("topic-firm-data-export", key, JSON.toJSONString(map));
 *   三个参数：topic、消息的 key、消息内容（JSON 字符串）。
 *
 * 【生产者职责】
 *   只负责「把消息投进信箱」，投完就走，不关心谁消费、什么时候消费。
 *   这就是「异步解耦」：发送方和接收方互不依赖。
 *
 * 【依赖】
 *   KafkaTemplate 是 spring-kafka 自动装配的（application.yml 里配了 bootstrap-servers），
 *   直接注入就能用，不需要自己 new。
 */
@Slf4j
@Component
public class OrderNoticeProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 构造注入（@RequiredArgsConstructor 也可，这里显式写出来便于学习）
    public OrderNoticeProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送订单通知消息
     *
     * @param message 订单通知内容
     */
    public void send(OrderNoticeMessage message) {
        try {
            // 消息内容转成 JSON 字符串（qingo 用 fastjson 的 JSON.toJSONString，
            // 本项目复用 Jackson，效果一样）
            String json = objectMapper.writeValueAsString(message);

            // key 用订单号：Kafka 会用 key 决定消息发到哪个分区，
            // 相同 key 的消息保证落在同一分区、按顺序消费（同一订单的消息有序）
            String key = message.getOrderNo();

            kafkaTemplate.send(KafkaTopics.ORDER_NOTICE, key, json);
            log.info("【Kafka-生产者】订单通知已发送: topic={}, key={}, value={}",
                    KafkaTopics.ORDER_NOTICE, key, json);
        } catch (Exception e) {
            log.error("【Kafka-生产者】消息发送失败: orderNo={}", message.getOrderNo(), e);
        }
    }
}
