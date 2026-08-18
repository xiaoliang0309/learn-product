package com.overseas.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务B 启动类（被调方）
 *
 * @EnableDiscoveryClient：把本服务注册到 Nacos（服务发现）。
 *   启动后，Nacos 控制台「服务列表」里就能看到 merchant-service-b。
 *   对齐 qingo 每个服务的启动类都有这个注解。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MerchantServiceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceBApplication.class, args);
    }
}
