package com.overseas.learning.dao;

import com.overseas.learning.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * TradeOrder DAO — MyBatis Mapper 接口
 *
 * 对应 trade_order 表（订单表），MySQL 侧的数据层
 * XML 映射文件在 resources/mapper/TradeOrderMapper.xml
 */
@Mapper
public interface TradeOrderMapper {

    /** 插入并返回自增 ID（id 会回填到 order 对象） */
    int insert(TradeOrder order);

    /** 按 ID 查询 */
    TradeOrder selectById(@Param("id") Long id);

    /** 查询列表（带条件筛选） */
    List<TradeOrder> selectList(TradeOrder order);

    /** 统计总数 */
    long count(TradeOrder order);

    /** 更新状态（演示支付/退款） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 删除 */
    int deleteById(@Param("id") Long id);
}
