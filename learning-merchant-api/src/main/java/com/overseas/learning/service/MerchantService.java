package com.overseas.learning.service;

import com.overseas.learning.entity.Merchant;

/**
 * 服务B 商户数据层 —— 只保留按 ID 查询（演示用）
 */
public interface MerchantService {

    /** 按 ID 查询商户 */
    Merchant getById(Long id);
}
