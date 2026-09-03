package com.overseas.learning.service;

import com.overseas.learning.dto.MerchantQueryDto;
import com.overseas.learning.dto.PageResult;
import com.overseas.learning.entity.Merchant;

/**
 * 商户「数据层」接口（Service 层，对齐 qingo orm-common 的 XxxService）
 *
 * 【这一层的职责】
 *   只封装 merchant 表的「增删改查」，不写任何业务判断:
 *     - 不做「邮箱是否重复」这类业务校验（那是 Biz 层的事）
 *     - 不做 DTO → Entity 转换（数据层只接收 Entity）
 *     - 一个方法对应一个（或少量）Mapper 调用
 *
 * 【qingo 命名约定（对照 MctShopService）】
 *   数据层方法名和 Dao 基本一致：
 *     查询单个  getById / getByEmail
 *     查询多个  getList / pageQuery（分页时额外组装 PageResult）
 *     写入      insert / update        ← 注意：qingo 用 insert，不叫 save
 *     删除      deleteById
 *
 * 【所在模块】
 *   本接口在 learning-orm-common 公共模块里（对齐 qingo-svem-biz-orm-common），
 *   被 learning-mct-api、learning-merchant-api 共同依赖复用。
 */
public interface MerchantService {

    /** 按邮箱查询（数据操作，供 Biz 层做唯一性校验） */
    Merchant getByEmail(String email);

    /** 按 ID 查询 */
    Merchant getById(Long id);

    /** 新增（qingo 风格叫 insert，只负责写入，不校验业务规则） */
    void insert(Merchant merchant);

    /** 更新（只负责写入） */
    void update(Merchant merchant);

    /** 按 ID 删除 */
    void deleteById(Long id);

    /** 分页查询（数据层负责组装分页结果） */
    PageResult<Merchant> pageQuery(MerchantQueryDto query);
}
