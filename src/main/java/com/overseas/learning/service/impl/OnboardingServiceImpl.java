package com.overseas.learning.service.impl;

import com.overseas.learning.dao.OnboardingRecordMapper;
import com.overseas.learning.entity.OnboardingRecord;
import com.overseas.learning.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 进件「数据层」实现
 *
 * 只负责 onboarding_record 表的读写，业务编排见 BizOnboardingServiceImpl。
 */
@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingRecordMapper onboardingRecordMapper;

    @Override
    public void save(OnboardingRecord record) {
        onboardingRecordMapper.insert(record);
    }

    @Override
    public void update(OnboardingRecord record) {
        onboardingRecordMapper.update(record);
    }

    @Override
    public OnboardingRecord getById(Long id) {
        return onboardingRecordMapper.selectById(id);
    }

    @Override
    public List<OnboardingRecord> listByMerchant(Long merchantId) {
        return onboardingRecordMapper.selectByMerchantId(merchantId);
    }
}
