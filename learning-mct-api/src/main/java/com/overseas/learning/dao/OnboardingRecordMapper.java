package com.overseas.learning.dao;

import com.overseas.learning.entity.OnboardingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OnboardingRecordMapper {

    int insert(OnboardingRecord record);

    OnboardingRecord selectById(@Param("id") Long id);

    List<OnboardingRecord> selectByMerchantId(@Param("merchantId") Long merchantId);

    int update(OnboardingRecord record);
}