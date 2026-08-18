package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务B —— 商户查询接口（被服务A 通过 Feign 调用）
 *
 * 这是「被调用方」，提供一个最简单的查询接口。
 * 服务A 会用 @FeignClient 按服务名调用这个接口。
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * GET /merchant/{id} — 按 ID 查商户（供服务A 远程调用）
     */
    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        log.info("【服务B】被调用了，查询商户 id={}", id);
        return Result.success(merchantService.getById(id));
    }
}
