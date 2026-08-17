package com.overseas.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // 开启定时任务（加了它，@Scheduled 才会生效）
public class OverseasLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(OverseasLearningApplication.class, args);
    }
}