package com.overseas.learning.dao;

import com.overseas.learning.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Merchant DAO — MyBatis Mapper 接口
 *
 * 注解 @Mapper 让 Spring 自动扫描并创建代理实现
 * XML 映射文件在 resources/mapper/MerchantMapper.xml
 */
@Mapper
public interface MerchantMapper {

    /** 插入并返回自增 ID（id 会回填到 merchant 对象） */
    int insert(Merchant merchant);

    /** 按 ID 查询 */
    Merchant selectById(@Param("id") Long id);

    /** 查询列表（带条件筛选） */
    List<Merchant> selectList(Merchant merchant);

    /** 统计总数 */
    long count(Merchant merchant);

    /** 更新 */
    int update(Merchant merchant);

    /** 删除（物理删除） */
    int deleteById(@Param("id") Long id);

    /** 按邮箱查询 */
    Merchant selectByEmail(@Param("email") String email);
}