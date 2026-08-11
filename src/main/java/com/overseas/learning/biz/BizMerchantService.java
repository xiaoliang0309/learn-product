package com.overseas.learning.biz;

import com.overseas.learning.dto.MerchantCreateDto;
import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;

/**
 * 商户「业务层」接口（Biz 层）
 *
 * 【为什么要加这一层？】
 *   真实企业项目（如 qingo）的调用链路是:
 *     Controller → BizXxxService → XxxService → Mapper
 *
 *   - Biz 层（本层）: 负责「业务逻辑」——校验业务规则、DTO↔Entity 转换、
 *     组合多个数据 Service、控制事务、调用外部接口（Feign）
 *   - Service 层（数据层）: 只负责「单表的增删改查」，不写业务判断
 *
 * 【命名规范】
 *   真实项目里 Biz 层的类名以 Biz 开头:
 *     BizMerchantService / BizGoodsBrandService / BizOrderService ...
 *
 * 【什么时候逻辑放 Biz 层，什么时候放数据层？】
 *   判断标准: 这段代码换了数据库表结构会不会变？
 *     - 「邮箱不能重复」是业务规则 → 放 Biz 层
 *     - 「按邮箱查一条记录」是数据操作 → 放数据层 Service
 *
 * @see com.overseas.learning.service.MerchantService 数据层接口
 */
public interface BizMerchantService {

    /**
     * 创建商户（含业务校验）
     *
     * 业务流程:
     *   1. 校验邮箱是否已存在（业务规则）
     *   2. DTO → Entity 转换
     *   3. 设置默认值
     *   4. 调用数据层写入数据库
     */
    Merchant create(MerchantCreateDto dto);

    /**
     * 按 ID 查询商户
     *
     * 说明: 简单的查询也可以直接在 Biz 层透传，真实项目中这里可能做
     *       权限判断、数据脱敏、关联查询组装等。
     */
    Merchant getById(Long id);

    /**
     * 分页查询商户列表
     */
    PageResult<Merchant> pageQuery(MerchantQueryDto query);

    /**
     * 更新商户（含业务校验）
     */
    Merchant update(Long id, MerchantCreateDto dto);

    /**
     * 删除商户
     */
    void delete(Long id);
}
