package com.overseas.learning.feign.fallback;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.feign.MerchantFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MerchantFeignClient 的降级兜底类 —— 对齐 qingo 的 Fallback 写法
 *
 * 【什么时候触发？】
 *   服务B 挂了、网络超时、调用失败时，Feign 不走真实调用，
 *   而是走这里的方法，返回一个「兜底结果」，避免接口直接报错崩掉。
 *
 * 【对齐 qingo】
 *   qingo 的 AscsInternalServiceFallback 也是这么写：
 *   implements 对应的 Feign 接口，每个方法返回一个错误兜底。
 */
@Slf4j
@Service
public class MerchantFeignFallback implements MerchantFeignClient {

    @Override
    public Result<Merchant> getById(Long id) {
        log.error("【Feign降级】调用 merchant-service-b 的 /merchant/{} 失败，走兜底", id);
        Result<Merchant> r = new Result<>();
        r.setCode(-1);
        r.setMsg("调用商户服务失败（服务B 可能未启动），已走降级兜底");
        r.setData(null);
        return r;
    }
}
