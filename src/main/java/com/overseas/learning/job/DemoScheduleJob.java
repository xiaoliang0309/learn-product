package com.overseas.learning.job;

import com.overseas.learning.dao.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务演示 —— 对齐 qingo 的 @Scheduled 用法
 *
 * 【什么是定时任务？】
 *   不用人触发，到了设定的时间点，程序自动执行。常用于「跑批」：
 *     - 每天凌晨对账（qingo 的 platformServiceFee 就是）
 *     - 每小时同步一次数据
 *     - 每分钟统计一次订单数
 *
 * 【怎么用？两步】
 *   1. 启动类加 @EnableScheduling（开启定时任务开关）
 *   2. 在方法上加 @Scheduled(cron = "...")，Spring 到点就自动调这个方法
 *
 * 【cron 表达式】6 位：秒 分 时 日 月 周
 *   "0 * * * * ?"      → 每分钟第 0 秒执行一次
 *   "0 0 2 * * ?"      → 每天凌晨 2 点
 *   "0 45 11 * * ?"    → 每天 11:45（qingo 的对账任务就是这么写的）
 *   记法：从左到右「秒分时日月周」，* 表示「每」，? 表示「不指定」
 *
 * 【@Async 是干嘛的】
 *   qingo 里 @Scheduled 常和 @Async 一起用。@Async 表示「另开一个线程异步执行」，
 *   不阻塞主线程。需要启动类加 @EnableAsync 才生效。
 *   学习项目为了聚焦，这里演示最基础的 @Scheduled 即可。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoScheduleJob {

    private final MerchantMapper merchantMapper;

    /**
     * 演示：每分钟统计一次商户总数
     *
     * cron = "0 * * * * ?" 表示「每分钟的第 0 秒」触发一次。
     * 你可以观察控制台，每分钟会准时打印一次日志。
     *
     * 真实项目里，这里会是：对账、同步第三方数据、清理过期数据、生成报表等。
     */
    @Scheduled(cron = "0 * * * * ?")
    public void countMerchant() {
        long total = merchantMapper.count(new com.overseas.learning.entity.Merchant());
        log.info("【定时任务】每分钟统计商户总数: total={}", total);
    }
}
