package com.overseas.learning.mq.rocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.common.RocketmqTopics;
import com.overseas.learning.dto.OrderNoticeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 分账完成通知「生产者」—— 把消息发到 RocketMQ
 *
 * 【对齐 qingo】qingo 分账完成后的真实写法（BizMctDivideOrderServiceImpl）：
 *   rocketMQTemplate.asyncSend("topic-order-divide-complete-mp",
 *       MessageBuilder.withPayload(divideOrderMap).build(),
 *       new RocketmqSendCallback(), 1000, 6);
 *
 * 【和 Kafka 生产者的三点不同（对照 OrderNoticeProducer 看）】
 *   1. 投递方式：RocketMQ 分 syncSend(同步等结果)/asyncSend(异步带回调)/sendOneWay(发完不管)；
 *      Kafka 基本就是 send() 异步。
 *   2. 消息体：RocketMQ 用 Spring 的 Message 对象（MessageBuilder.withPayload 包一层），
 *      Kafka 直接传 value 字符串。
 *   3. 发送时机：RocketMQTemplate 不是启动就自动装配好的（尤其在某些版本），
 *      常用 @PostConstruct 或延迟到第一次用时再拿 —— qingo 就是这么干的，这里同样处理。
 *
 * 【开关】@ConditionalOnProperty("spring.rocketmq.enabled=true")：
 *   没配 / 配 false 时这个 Bean 根本不创建，本机没装 RocketMQ 也能正常启动。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rocketmq.enabled", havingValue = "true")
public class DivideOrderProducer {

    /**
     * 不用构造注入，改 @Resource 字段注入 + 懒加载：
     * 避免 Spring 启动时就急着初始化 RocketMQTemplate（连不上 NameServer 会卡住），
     * 对齐 qingo 里「用到再取」的写法。
     */
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 同步发送分账完成消息（演示最直观的写法）
     *
     * @param message 订单/分账内容
     */
    public void send(OrderNoticeMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            // RocketMQ 消息体要包成 Spring Message 对象
            Message<String> msg = MessageBuilder.withPayload(json).build();

            // convertAndSend = 同步发送，发完等 Broker 确认才返回（最像 Kafka 的 send）
            rocketMQTemplate.convertAndSend(RocketmqTopics.ORDER_DIVIDE_COMPLETE, msg);
            log.info("【RocketMQ-生产者】分账完成消息已发送: topic={}, value={}",
                    RocketmqTopics.ORDER_DIVIDE_COMPLETE, json);
        } catch (Exception e) {
            log.error("【RocketMQ-生产者】发送失败: orderNo={}", message.getOrderNo(), e);
        }
    }
}
