package com.overseas.learning.mq.rocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.common.RocketmqTopics;
import com.overseas.learning.dto.OrderNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 分账完成通知「消费者」—— 监听 RocketMQ，收到消息异步处理
 *
 * 【对齐 qingo】qingo 分账消费者的真实写法（MctDivideOrderMQListener）：
 *   @Component
 *   @RocketMQMessageListener(consumerGroup = "xxx", topic = "topic-order-divide-complete-mp")
 *   public class MctDivideOrderMQListener implements RocketMQListener<String> {
 *       @Override
 *       public void onMessage(String message) { ... JSON.parseObject(message, XxxRequest.class) ... }
 *   }
 *
 * 【和 Kafka 消费者的四点不同（对照 OrderNoticeListener 看）】
 *   1. 注解位置：RocketMQ 的 @RocketMQMessageListener 标在「类」上，类实现 RocketMQListener<T>；
 *      Kafka 的 @KafkaListener 标在「方法」上，一个类可以监听多个 topic。
 *   2. 确认机制：RocketMQ 方法正常返回=消费成功，抛异常=自动重试（默认16次，递进间隔），
 *      超过进死信队列 DLQ；Kafka 要手动 ack.acknowledge()，不确认就重投。
 *   3. 消息体：RocketMQ 直接拿到泛型 T（这里是 String 的 JSON）；Kafka 拿到 ConsumerRecord 再 .value()。
 *   4. consumerGroup 在注解里写死；Kafka 的 groupId 也常在注解，但学习项目读的是 yml。
 *
 * 【开关】同生产者：spring.rocketmq.enabled=false 时不创建，不连真服务也能启动。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rocketmq.enabled", havingValue = "true")
@RocketMQMessageListener(
        consumerGroup = "overseas-learning-divide-group",
        topic = RocketmqTopics.ORDER_DIVIDE_COMPLETE
)
public class DivideOrderListener implements RocketMQListener<String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(String message) {
        log.info("【RocketMQ-消费者】收到分账完成消息: {}", message);
        try {
            // 1. JSON 字符串解析回对象（qingo 用 JSON.parseObject，本项目用 Jackson）
            OrderNoticeMessage msg = objectMapper.readValue(message, OrderNoticeMessage.class);

            // 2. 处理业务（真实项目这里是「记账/给商户结算」，这里用日志模拟）
            doDivide(msg);

            // 3. 方法正常结束 = 消费成功（RocketMQ 自动确认，无需手动 ack）
            log.info("【RocketMQ-消费者】分账处理完成: orderNo={}", msg.getOrderNo());

        } catch (Exception e) {
            // 抛出异常 → RocketMQ 自动重试（越重试间隔越长），16 次后进入死信队列 DLQ
            log.error("【RocketMQ-消费者】处理失败，将自动重试: message={}", message, e);
            throw new RuntimeException("分账处理失败，触发 RocketMQ 重试", e);
        }
    }

    /**
     * 模拟「分账/结算」业务：真实项目里是把平台服务费、商户应得分别记账
     */
    private void doDivide(OrderNoticeMessage msg) {
        log.info("【RocketMQ-消费者】>>> 模拟分账：订单 {} 给商户【{}】结算（异步+可靠投递，失败自动重试）",
                msg.getOrderNo(), msg.getMerchantName());
    }
}
