package com.overseas.learning.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job 执行器配置 —— 对齐 qingo 接入方式（官方原生包）
 *
 * 【xxl-job 的两个角色】
 *   调度中心（xxl-job-admin）：独立部署的网页控制台，负责「到点触发任务、管理任务」
 *   执行器（你的项目）：本配置类注册的东西，负责「真正执行任务的代码」
 *
 * 【它们怎么配合】
 *   调度中心到点了 → 通过 HTTP 回调你项目的执行器端口(9999) → 触发你写的 @XxlJob 方法
 *
 * 【这个配置类干嘛的】
 *   把「执行器」注册成一个 Spring Bean，项目启动时它会：
 *     1. 连接调度中心（addresses）
 *     2. 把自己登记上去（appname + 本机地址）
 *     3. 监听 9999 端口，等调度中心来回调触发任务
 */
@Slf4j
@Configuration
public class XxlJobConfig {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address}")
    private String address;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("【xxl-job】执行器初始化: appname={}, admin={}", appname, adminAddresses);
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAccessToken(accessToken);
        executor.setAppname(appname);
        executor.setAddress(address);
        executor.setIp(ip);
        executor.setPort(port);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        return executor;
    }
}
