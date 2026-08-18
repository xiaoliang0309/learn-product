package com.overseas.learning.service.impl;

import com.overseas.learning.dao.MerchantMapper;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 服务B 商户数据层实现 —— 只保留按 ID 查询
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    @Override
    public Merchant getById(Long id) {
        return merchantMapper.selectById(id);
    }
}
