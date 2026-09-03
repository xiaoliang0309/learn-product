package com.overseas.learning.dao;

import com.overseas.learning.entity.OnboardingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OnboardingRecordMapper {

    int insert(@Param("pojo") OnboardingRecord pojo);

    OnboardingRecord getById(@Param("id") Long id);

    List<OnboardingRecord> getByMerchantId(@Param("merchantId") Long merchantId);

    int update(@Param("pojo") OnboardingRecord pojo);
}