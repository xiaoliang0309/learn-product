package com.overseas.learning.controller;

import com.overseas.learning.biz.BizMerchantService;
import com.overseas.learning.common.Result;
import com.overseas.learning.dto.MerchantCreateDto;
import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商户管理 Controller
 *
 * 【调用链路（改造后）】
 *   MerchantController → BizMerchantService → MerchantService → MerchantMapper
 *
 * 【Controller 的职责】
 *   1. 接收 HTTP 请求，解析参数
 *   2. @Valid 触发参数校验
 *   3. 调用 Biz 层（注意：不是数据层 Service）
 *   4. 返回统一 Result
 *
 * Controller 不写业务逻辑，也不直接调数据层。
 * 真实项目（qingo）中 Controller 还会做:
 *   - @CurrentUser 解析当前登录用户
 *   - 权限校验（@PreAuthorize 或自定义拦截器）
 */
@Slf4j
@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    // 注入的是「业务层」BizMerchantService，不是数据层 MerchantService
    private final BizMerchantService bizMerchantService;

    /**
     * POST /api/merchants — 创建商户
     */
    @PostMapping
    public Result<Merchant> create(@Valid @RequestBody MerchantCreateDto dto) {
        Merchant merchant = bizMerchantService.create(dto);
        return Result.success(merchant);
    }

    /**
     * GET /api/merchants/{id} — 查询单个商户
     */
    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        return Result.success(bizMerchantService.getById(id));
    }

    /**
     * GET /api/merchants — 分页查询商户列表
     *
     * ?page=1&size=20&fullName=xxx&bizType=1&status=1
     */
    @GetMapping
    public Result<PageResult<Merchant>> pageQuery(MerchantQueryDto query) {
        return Result.success(bizMerchantService.pageQuery(query));
    }

    /**
     * PUT /api/merchants/{id} — 更新商户
     */
    @PutMapping("/{id}")
    public Result<Merchant> update(@PathVariable Long id, @Valid @RequestBody MerchantCreateDto dto) {
        return Result.success(bizMerchantService.update(id, dto));
    }

    /**
     * DELETE /api/merchants/{id} — 删除商户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bizMerchantService.delete(id);
        return Result.success();
    }
}
