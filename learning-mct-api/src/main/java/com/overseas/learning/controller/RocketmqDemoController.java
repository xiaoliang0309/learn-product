package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.dto.OrderNoticeMessage;
import com.overseas.learning.mq.rocket.DivideOrderProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * RocketMQ 演示 Controller —— 对齐 qingo 的「分账完成异步通知」场景
 *
 * 【和 OrderController(Kafka) 的关系】
 *   同样是「下单后发一条异步消息」，这里换成 RocketMQ 投递，
 *   让你直观对比两种 MQ 的写法差异（详见 DivideOrderProducer/Listener 注释）。
 *
 * 【开关说明】
 *   本机默认没装 RocketMQ（spring.rocketmq.enabled=false），
 *   此时 DivideOrderProducer 这个 Bean 根本不存在 —— 所以这里用
 *   ObjectProvider 按需取，取不到就返回「未开启」的友好提示，而不是启动报错。
 *
 *   想连真服务：本机起 NameServer(9876)+Broker，把 application.yml 的
 *   spring.rocketmq.enabled 改 true，重启后再点按钮即可真实收发。
 */
@Slf4j
@RestController
@RequestMapping("/api/rocketmq")
public class RocketmqDemoController {

    /**
     * ObjectProvider = 「可有可无」的注入方式：
     * 开关开着(enabled=true)时能拿到 DivideOrderProducer；关着时拿到的是「空」，不报错。
     */
    private final ObjectProvider<DivideOrderProducer> producerProvider;

    public RocketmqDemoController(ObjectProvider<DivideOrderProducer> producerProvider) {
        this.producerProvider = producerProvider;
    }

    /**
     * POST /api/rocketmq/divide — 下单并触发「分账完成」RocketMQ 消息
     */
    @PostMapping("/divide")
    public Result<String> divide(@RequestParam(defaultValue = "1") Long merchantId,
                                 @RequestParam(defaultValue = "测试商户") String merchantName) {
        DivideOrderProducer producer = producerProvider.getIfAvailable();
        if (producer == null) {
            // 开关关闭：不连真服务，返回提示（前端 Tab 会展示这句话）
            return Result.error(500, "RocketMQ 未开启（spring.rocketmq.enabled=false）。"
                    + "本演示默认纯看代码不连真服务；"
                    + "想真实收发请本机起 NameServer+Broker 并把开关改 true。");
        }

        // 开关开着：走真实 RocketMQ 发送
        String orderNo = "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        OrderNoticeMessage message = OrderNoticeMessage.builder()
                .orderNo(orderNo)
                .merchantId(merchantId)
                .merchantName(merchantName)
                .createTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        producer.send(message);
        log.info("【订单】下单成功，已发 RocketMQ 分账完成消息: orderNo={}", orderNo);
        return Result.success("已发送 RocketMQ 分账消息: " + orderNo);
    }
}
