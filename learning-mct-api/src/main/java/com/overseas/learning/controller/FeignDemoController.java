package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.feign.MerchantFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Feign 演示 Controller —— 服务A 通过 Feign 调用服务B
 *
 * 【演示什么？】
 *   服务A（本项目，9090）自己不查数据库，而是通过 Feign 远程调用
 *   服务B（merchant-service-b，9093）的 /merchant/{id} 接口拿数据。
 *   这就是微服务之间的调用。
 *
 * 【测试方法】
 *   1. 启动 Nacos、服务B、服务A
 *   2. 访问 http://localhost:9090/api/feign/merchant/1
 *   3. 服务A 会通过 Feign 调到服务B，返回商户数据
 *   4. 如果停掉服务B 再访问，会走 fallback 降级返回兜底提示
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class FeignDemoController {

    // 注入 Feign 客户端（Feign 自动生成实现，按服务名调远程）
    private final MerchantFeignClient merchantFeignClient;

    /**
     * GET /merchant/remote/{id}
     * 服务A 通过 Feign 调用服务B 查询商户
     * （URL 用业务名，不含技术词 feign，对齐 qingo 风格）
     */
    @GetMapping("/remote/{id}")
    public Result<Merchant> getMerchant(@PathVariable Long id) {
        log.info("【服务A】收到请求，准备通过 Feign 调用服务B 查询商户 id={}", id);
        Result<Merchant> result = merchantFeignClient.getById(id);
        log.info("【服务A】Feign 调用服务B 返回: code={}, msg={}", result.getCode(), result.getMsg());
        return result;
    }
}
