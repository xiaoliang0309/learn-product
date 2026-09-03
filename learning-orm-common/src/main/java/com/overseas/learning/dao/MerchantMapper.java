package com.overseas.learning.dao;

import com.overseas.learning.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Merchant DAO — MyBatis Mapper 接口（对齐 qingo orm-common 的 XxxDao 命名约定）
 *
 * 【qingo 命名约定（对照 MctShopDao）】
 *   查询单个：getById / getByEmail        （get + By + 字段，返回单个实体）
 *   查询多个：getList / getByXxxList      （返回 List）
 *   写入：    insert / update / batchInsert
 *   删除：    deleteById
 *   参数注解：实体参数统一 @Param("pojo")，标量参数 @Param("字段名")
 *
 * 注解 @Mapper 让 Spring 自动扫描并创建代理实现，
 * XML 映射文件在本模块 resources/mapper/MerchantMapper.xml
 */
@Mapper
public interface MerchantMapper {

    /** 插入并返回自增 ID（id 会回填到 merchant 对象） */
    int insert(@Param("pojo") Merchant pojo);

    /** 按 ID 查询（qingo 风格：getById，不再叫 selectById） */
    Merchant getById(@Param("id") Long id);

    /** 查询列表（带条件筛选，qingo 风格：getList） */
    List<Merchant> getList(@Param("pojo") Merchant pojo);

    /** 统计总数 */
    long count(@Param("pojo") Merchant pojo);

    /** 更新 */
    int update(@Param("pojo") Merchant pojo);

    /** 删除（物理删除） */
    int deleteById(@Param("id") Long id);

    /** 按邮箱查询（qingo 风格：getByEmail） */
    Merchant getByEmail(@Param("email") String email);
}
