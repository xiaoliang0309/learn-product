package com.overseas.learning.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.overseas.learning.dao.MerchantMapper;
import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;
import com.overseas.learning.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商户「数据层」实现（对齐 qingo orm-common 的 XxxServiceImpl）
 *
 * 【职责】只保留「怎么查、怎么写」的纯数据操作：
 *   - 数据层方法一般不加 @Transactional
 *     事务由调用它的 Biz 层方法控制（事务传播 REQUIRED，默认加入已有事务）
 *   - 数据层不做业务校验，即使查到 null 也直接返回，
 *     「不存在怎么办」由 Biz 层决定（抛异常还是返回默认值）
 *
 * 【所在模块】learning-orm-common（对齐 qingo-svem-biz-orm-common），多服务共享。
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    @Override
    public Merchant getByEmail(String email) {
        return merchantMapper.getByEmail(email);
    }

    @Override
    public Merchant getById(Long id) {
        // 数据层只负责查，查不到返回 null
        // 「查不到算不算错误」由 Biz 层判断
        return merchantMapper.getById(id);
    }

    @Override
    public void insert(Merchant merchant) {
        merchantMapper.insert(merchant);
    }

    @Override
    public void update(Merchant merchant) {
        merchantMapper.update(merchant);
    }

    @Override
    public void deleteById(Long id) {
        merchantMapper.deleteById(id);
    }

    @Override
    public PageResult<Merchant> pageQuery(MerchantQueryDto query) {
        // PageHelper 分页: 在查询之前调用，会拦截接下来的第一条 SQL 自动拼接 LIMIT
        // 线程安全：PageHelper 用 ThreadLocal 存储分页参数，查询完自动清除
        PageHelper.startPage(query.getPage(), query.getSize());

        // 组装查询条件
        Merchant condition = new Merchant();
        condition.setFullName(query.getFullName());
        condition.setBizType(query.getBizType());
        condition.setStatus(query.getStatus());

        List<Merchant> list = merchantMapper.getList(condition);
        long total = ((Page<Merchant>) list).getTotal();

        return PageResult.of(list, total, query.getPage(), query.getSize());
    }
}
