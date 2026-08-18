package com.overseas.learning.service;

import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;

/**
 * 商户「数据层」接口（Service 层）
 *
 * 【这一层的职责】
 *   只封装 merchant 表的「增删改查」，不写任何业务判断:
 *     - 不做「邮箱是否重复」这类业务校验（那是 Biz 层的事）
 *     - 不做 DTO → Entity 转换（数据层只接收 Entity）
 *     - 一个方法对应一个（或少量）Mapper 调用
 *
 * 【为什么不直接让 Biz 层调 Mapper？】
 *   1. 数据层可以做一些「纯数据」的封装，比如分页参数处理、结果判空
 *   2. 真实项目中，数据层可能集成 MyBatis-Plus、缓存等，Biz 层不需要感知
 *   3. 单元测试时可以只 mock 数据层，不用关心 SQL 细节
 *
 * 【命名】
 *   真实项目里数据层接口就叫 XxxService（不带 Biz 前缀），
 *   比如 MerchantService、GoodsBrandService。
 */
public interface MerchantService {

    /**
     * 按邮箱查询（数据操作，供 Biz 层做唯一性校验）
     */
    Merchant getByEmail(String email);

    /**
     * 按 ID 查询
     */
    Merchant getById(Long id);

    /**
     * 新增（只负责写入，不校验业务规则）
     */
    void save(Merchant merchant);

    /**
     * 更新（只负责写入）
     */
    void update(Merchant merchant);

    /**
     * 按 ID 删除
     */
    void deleteById(Long id);

    /**
     * 分页查询（数据层负责组装分页结果）
     */
    PageResult<Merchant> pageQuery(MerchantQueryDto query);
}
