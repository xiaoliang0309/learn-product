package com.overseas.learning.biz;

import com.overseas.learning.dto.OnboardingSubmitDto;
import com.overseas.learning.entity.OnboardingRecord;

import java.util.List;

/**
 * 进件「业务层」接口（Biz 层）
 *
 * 【Biz 层价值体现得最清楚的地方】
 *   进件不是简单的单表 CRUD，它是一个「多步骤业务流程」:
 *     1. 校验商户是否存在且状态正常
 *     2. 校验该商户是否已有进行中的进件（防重复提交）
 *     3. 保存进件草稿
 *     4. 调用外部「支付中台」接口 → 拿申请单号
 *     5. 更新进件状态为「审核中」
 *
 *   这一整套编排逻辑必须放在 Biz 层，用 @Transactional 包住，
 *   任何一步失败整体回滚。数据层 Service 只负责单表的存取。
 */
public interface BizOnboardingService {

    /**
     * 提交进件申请（完整业务流程）
     *
     * @param dto      进件表单
     * @param operator 操作人（真实项目从 @CurrentUser 取）
     */
    OnboardingRecord submit(OnboardingSubmitDto dto, String operator);

    /**
     * 查询进件详情
     */
    OnboardingRecord getById(Long id);

    /**
     * 查询某商户的进件记录列表
     */
    List<OnboardingRecord> listByMerchant(Long merchantId);

    /**
     * 模拟支付中台回调审核结果
     *
     * @param approved true=通过 false=驳回
     * @param remark   备注 / 驳回原因
     */
    void simulateCallback(Long id, boolean approved, String remark);
}
