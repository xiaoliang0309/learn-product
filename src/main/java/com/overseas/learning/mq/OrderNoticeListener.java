package com.overseas.learning.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.common.KafkaTopics;
import com.overseas.learning.dto.OrderNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 订单通知「消费者」—— 监听 Kafka，收到消息后异步处理
 *
 * 【对齐 qingo】
 *   qingo 的消费写法（FirmDataExportHandleListener）：
 *     @KafkaListener(topics = "topic-firm-data-export")
 *     public void execute(ConsumerRecord<?, ?> record, Acknowledgment ack) {
 *         XxxRequest req = JSON.parseObject(record.value().toString(), XxxRequest.class);
 *         // ... 处理业务
 *         ack.acknowledge();   // 手动确认：告诉 Kafka 这条消息处理完了
 *     }
 *
 * 【关键概念】
 *   @KafkaListener：标在方法上，Spring 就会持续监听这个 topic，
 *                   一有消息进来就自动调用这个方法。
 *   ConsumerRecord ：收到的一条消息（含 topic/key/value）。
 *   Acknowledgment ：手动确认。处理成功后调 ack.acknowledge()，
 *                    Kafka 才把这条消息标记为「已消费」；
 *                    如果没确认（比如处理报错），消息会再次投递，保证不丢。
 *
 * 【异步的体现】
 *   生产者发完消息就返回了；这个方法是「另一条线程」在后台被触发的，
 *   和发消息的请求不在同一个流程里 —— 这就是「异步」。
 */
@Slf4j
@Component
public class OrderNoticeListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = KafkaTopics.ORDER_NOTICE, groupId = "overseas-learning-group")
    public void onMessage(ConsumerRecord<?, ?> record, Acknowledgment ack) {
        log.info("【Kafka-消费者】收到消息: topic={}, key={}", record.topic(), record.key());

        try {
            // 1. 把 JSON 字符串解析回消息对象（qingo 用 JSON.parseObject，本项目用 Jackson）
            OrderNoticeMessage message = objectMapper.readValue(record.value().toString(), OrderNoticeMessage.class);

            // 2. 处理业务（这里用日志模拟「发送通知」，真实项目里可能是发短信/推送/记账）
            doNotify(message);

            // 3. 处理成功 → 手动确认，告诉 Kafka 这条消息消费完成
            ack.acknowledge();
            log.info("【Kafka-消费者】消息处理完成并已确认: orderNo={}", message.getOrderNo());

        } catch (Exception e) {
            // 处理失败：不调用 ack.acknowledge()，Kafka 之后会重新投递这条消息
            log.error("【Kafka-消费者】消息处理失败，等待重试: value={}", record.value(), e);
        }
    }

    /**
     * 模拟「发送订单通知」的业务逻辑
     * 真实项目里这里可能是：发短信、App 推送、写通知表、同步到其他系统
     */
    private void doNotify(OrderNoticeMessage message) {
        log.info("【Kafka-消费者】>>> 模拟发送通知：商户【{}】有新订单 {}，下单时间 {}",
                message.getMerchantName(), message.getOrderNo(), message.getCreateTime());
    }
}
