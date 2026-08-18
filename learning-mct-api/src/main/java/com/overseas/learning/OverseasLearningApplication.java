package com.overseas.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling       // 开启定时任务（@Scheduled 生效）
@EnableDiscoveryClient  // 注册到 Nacos（服务发现）
@EnableFeignClients     // 开启 Feign（扫描 @FeignClient 接口，服务间调用）
public class OverseasLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(OverseasLearningApplication.class, args);
    }
}