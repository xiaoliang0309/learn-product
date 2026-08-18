package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.dto.OrderNoticeMessage;
import com.overseas.learning.mq.OrderNoticeProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 订单 Controller —— 演示 Kafka 的入口
 *
 * 【这是学习专用接口，演示「下单 → 发 Kafka → 异步消费」】
 *
 * 流程：
 *   1. 前端调 POST /api/orders?merchantId=1&merchantName=测试商户
 *   2. 接口生成一个订单号，立刻「发 Kafka 消息」
 *   3. 接口马上返回（不等通知发完）—— 这就是异步
 *   4. 后台 OrderNoticeListener 收到消息，慢慢「发通知」（看控制台日志）
 *
 * 你观察的重点：
 *   接口返回很快（发消息是毫秒级），而「发通知」在后台异步进行。
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderNoticeProducer orderNoticeProducer;

    /**
     * POST /api/orders — 下单（演示 Kafka 异步通知）
     *
     * @param merchantId   商户ID
     * @param merchantName 商户名称
     */
    @PostMapping
    public Result<String> createOrder(@RequestParam Long merchantId,
                                      @RequestParam String merchantName) {
        // 1. 生成订单号（真实项目会写入订单表，这里聚焦 Kafka 演示，省略入库）
        String orderNo = "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. 组装消息体
        OrderNoticeMessage message = OrderNoticeMessage.builder()
                .orderNo(orderNo)
                .merchantId(merchantId)
                .merchantName(merchantName)
                .createTime(createTime)
                .build();

        // 3. ★ 发 Kafka 消息（发完就返回，不等消费）
        orderNoticeProducer.send(message);

        log.info("【订单】下单成功，已发 Kafka 通知消息: orderNo={}", orderNo);

        // 4. 立刻返回，不等「发通知」完成 —— 异步的体现
        return Result.success(orderNo);
    }
}
