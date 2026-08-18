package com.overseas.learning.feign;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.feign.fallback.MerchantFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商户 Feign 客户端 —— 对齐 qingo 的 @FeignClient 写法
 *
 * 【这是干嘛的？】
 *   声明「我要调用 merchant-service-b 这个服务的 /merchant/{id} 接口」。
 *   你只写接口，Feign 帮你生成实现，调用时像调本地方法一样。
 *
 * 【name = "merchant-service-b"】
 *   就是服务B 在 Nacos 里注册的服务名（服务B 的 spring.application.name）。
 *   Feign 拿这个名字去 Nacos 查服务B 的地址，再发 HTTP 请求。
 *
 * 【fallback = MerchantFeignFallback.class】
 *   降级兜底：如果服务B 挂了/调用失败，就走 fallback 里的方法返回兜底结果，
 *   不会直接报错。对齐 qingo 的 AscsInternalServiceFallback。
 *
 * 【方法上的注解】
 *   @GetMapping("/merchant/{id}") 要和服务B 的真实接口路径一致。
 */
@FeignClient(name = "merchant-service-b", fallback = MerchantFeignFallback.class)
public interface MerchantFeignClient {

    /**
     * 调用服务B 的 GET /merchant/{id}
     */
    @GetMapping("/merchant/{id}")
    Result<Merchant> getById(@PathVariable("id") Long id);
}
