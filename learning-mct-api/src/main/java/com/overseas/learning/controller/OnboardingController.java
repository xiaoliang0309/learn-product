package com.overseas.learning.controller;

import com.overseas.learning.biz.BizOnboardingService;
import com.overseas.learning.common.Result;
import com.overseas.learning.dto.OnboardingSubmitDto;
import com.overseas.learning.entity.OnboardingRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 进件管理 Controller
 *
 * 调用链路: Controller → BizOnboardingService → OnboardingService → Mapper
 *
 * 模拟进件流程:
 *   1. 商户提交进件资料 → POST /api/onboarding
 *   2. 查询进件状态 → GET /api/onboarding/{id}
 *   3. 模拟支付中台回调 → POST /api/onboarding/{id}/callback
 */
@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    // 注入「业务层」BizOnboardingService
    private final BizOnboardingService bizOnboardingService;

    /**
     * POST /api/onboarding — 提交进件申请
     */
    @PostMapping
    public Result<OnboardingRecord> submit(@Valid @RequestBody OnboardingSubmitDto dto) {
        // 模拟从请求上下文获取操作人（真实项目用 @CurrentUser AuthWorker 获取）
        String operator = "admin";
        return Result.success(bizOnboardingService.submit(dto, operator));
    }

    /**
     * GET /api/onboarding/{id} — 查询进件详情
     */
    @GetMapping("/{id}")
    public Result<OnboardingRecord> getById(@PathVariable Long id) {
        return Result.success(bizOnboardingService.getById(id));
    }

    /**
     * GET /api/onboarding?merchantId=1 — 查询商户的进件记录
     */
    @GetMapping
    public Result<List<OnboardingRecord>> listByMerchant(@RequestParam Long merchantId) {
        return Result.success(bizOnboardingService.listByMerchant(merchantId));
    }

    /**
     * POST /api/onboarding/{id}/callback — 模拟支付中台回调
     *
     * 参数:
     *   approved: true=通过 false=驳回
     *   remark: 备注/驳回原因
     */
    @PostMapping("/{id}/callback")
    public Result<Void> callback(@PathVariable Long id,
                                 @RequestParam boolean approved,
                                 @RequestParam(defaultValue = "") String remark) {
        bizOnboardingService.simulateCallback(id, approved, remark);
        return Result.success();
    }
}
