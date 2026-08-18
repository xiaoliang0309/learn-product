package com.overseas.learning.service;

import com.overseas.learning.entity.OnboardingRecord;

import java.util.List;

/**
 * 进件「数据层」接口（Service 层）
 *
 * 只封装 onboarding_record 表的增删改查，
 * 进件流程的「业务编排」（校验商户、防重复提交、调用支付中台）在 Biz 层。
 *
 * @see com.overseas.learning.biz.BizOnboardingService 业务层
 */
public interface OnboardingService {

    /**
     * 新增进件记录
     */
    void save(OnboardingRecord record);

    /**
     * 更新进件记录
     */
    void update(OnboardingRecord record);

    /**
     * 按 ID 查询
     */
    OnboardingRecord getById(Long id);

    /**
     * 按商户 ID 查询进件记录列表
     */
    List<OnboardingRecord> listByMerchant(Long merchantId);
}
