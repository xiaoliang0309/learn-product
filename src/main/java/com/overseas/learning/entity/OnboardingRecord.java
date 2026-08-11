package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 进件记录实体 — 对应 onboarding_record 表
 *
 * 注解说明（与 Merchant 相同，对标 qingo）:
 *   @Data + @Builder + @NoArgsConstructor + @AllArgsConstructor
 *   支持 Builder 链式构建，同时保留 MyBatis 需要的无参构造
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRecord {
    private Long id;
    private Long merchantId;
    private Integer receiveType;
    private Integer paymentType;
    private String applyNo;
    private String channelMctNo;
    private String formData;
    private Integer applyStatus;
    private String remark;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
}