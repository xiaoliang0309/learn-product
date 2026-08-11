package com.overseas.learning.biz.impl;

import com.overseas.learning.biz.BizOnboardingService;
import com.overseas.learning.common.BusinessException;
import com.overseas.learning.common.ResultCode;
import com.overseas.learning.dto.OnboardingSubmitDto;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.entity.OnboardingRecord;
import com.overseas.learning.service.MerchantService;
import com.overseas.learning.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 进件「业务层」实现（Biz 层）
 *
 * 【这个类最能体现 Biz 层的价值】
 *   它同时注入了「商户数据层」和「进件数据层」两个 Service，
 *   在一个方法里编排多步操作，组成一个完整业务。
 *
 * 【真实项目对照】
 *   qingo 项目里，进件流程的 Biz 层还会注入:
 *     - RedisService      → 防重复提交、缓存审核状态
 *     - Feign 客户端       → 真正调用支付中台 HTTP 接口
 *     - RocketMQ / Kafka   → 进件成功后发消息通知其他系统
 *   学习项目为了聚焦，只保留了「编排 + 事务」这个核心思想。
 *
 * 【事务边界】
 *   @Transactional 加在 Biz 层方法上，意味着:
 *     submit() 里的所有数据库操作（写进件表、更新状态）要么全部成功，要么全部回滚。
 *   如果事务注解加在数据层，跨多个数据层方法的业务就无法保证原子性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizOnboardingServiceImpl implements BizOnboardingService {

    // 注入多个「数据层」Service，Biz 层负责编排它们
    private final OnboardingService onboardingService;
    private final MerchantService merchantService;

    @Override
    @Transactional
    public OnboardingRecord submit(OnboardingSubmitDto dto, String operator) {
        // ========== 1. 业务前置校验 ==========
        // 校验商户存在且状态正常（跨表校验，必须在 Biz 层做）
        Merchant merchant = merchantService.getById(dto.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_FOUND);
        }
        if (merchant.getStatus() != null && merchant.getStatus() == 0) {
            throw new BusinessException(1004, "商户已禁用，无法进件");
        }

        // 校验是否已有「审核中」的进件（防重复提交的业务规则）
        List<OnboardingRecord> existing = onboardingService.listByMerchant(dto.getMerchantId());
        boolean hasInProgress = existing.stream()
                .anyMatch(r -> r.getApplyStatus() != null && r.getApplyStatus() == 1);
        if (hasInProgress) {
            throw new BusinessException(2004, "该商户已有审核中的进件，请勿重复提交");
        }

        // ========== 2. 保存进件草稿（★ 用 Builder 构建，与 qingo 一致）==========
        // 初始状态 applyStatus=0（草稿）、createdBy=操作人，直接在构建时写死
        OnboardingRecord record = OnboardingRecord.builder()
                .merchantId(dto.getMerchantId())
                .receiveType(dto.getReceiveType())
                .paymentType(dto.getPaymentType())
                .formData(dto.getFormData())
                .applyStatus(0) // 草稿
                .createdBy(operator)
                .build();
        onboardingService.save(record);
        log.info("【Biz】进件草稿创建成功: id={}", record.getId());

        // ========== 3. 调用支付中台（模拟） ==========
        // 真实项目里这一步是 Feign HTTP 调用，可能抛超时/网络异常
        // 因为方法上有 @Transactional，抛异常会导致第 2 步的草稿也回滚
        String applyNo = "AP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        record.setApplyNo(applyNo);
        record.setApplyStatus(1); // 审核中
        onboardingService.update(record);

        log.info("【Biz】支付中台进件成功: id={}, applyNo={}", record.getId(), applyNo);
        return record;
    }

    @Override
    public OnboardingRecord getById(Long id) {
        OnboardingRecord record = onboardingService.getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.ONBOARDING_NOT_FOUND);
        }
        return record;
    }

    @Override
    public List<OnboardingRecord> listByMerchant(Long merchantId) {
        return onboardingService.listByMerchant(merchantId);
    }

    @Override
    @Transactional
    public void simulateCallback(Long id, boolean approved, String remark) {
        OnboardingRecord record = getById(id);

        // 业务规则: 只有「审核中」状态才能回调
        if (record.getApplyStatus() == null || record.getApplyStatus() != 1) {
            throw new BusinessException(2005, "当前状态不允许回调，仅审核中的进件可操作");
        }

        if (approved) {
            String channelMctNo = "MCT" + System.currentTimeMillis();
            record.setChannelMctNo(channelMctNo);
            record.setApplyStatus(2); // 已入驻
            record.setRemark(remark);
            log.info("【Biz】进件审核通过: id={}, channelMctNo={}", id, channelMctNo);
        } else {
            record.setApplyStatus(3); // 驳回
            record.setRemark(remark);
            log.info("【Biz】进件驳回: id={}, reason={}", id, remark);
        }

        onboardingService.update(record);
    }
}
