package com.overseas.learning.biz;

import com.overseas.learning.dto.OrderCreateDto;
import com.overseas.learning.entity.TradeOrder;

import java.util.List;
import java.util.Map;

/**
 * 订单双写业务接口 —— 演示「下单时同时写 MySQL + ES」
 *
 * 【对齐 qingo】
 *   qingo 的真实流程（简化）：
 *     BizMctOrderServiceImpl.createOrder() {
 *         orderMapper.insert(order);              // 1. 写 MySQL（主数据，事务保证）
 *         docEsMctOrderService.save(esDoc);       // 2. 同时写 ES（搜索副本）
 *     }
 *
 *   之后：
 *     搜索 → 只查 ES（快，倒排索引）
 *     详情 → 查 MySQL（完整数据）
 *     定时 → XXL-JOB 把 MySQL 全量同步到 ES，修正不一致
 */
public interface BizOrderDualService {

    /**
     * 下单（★ 核心：同时写 MySQL + ES）
     *
     * @param dto 下单参数（DTO），Biz 层内部转成 TradeOrder 实体
     * @return 写入后的订单（含 MySQL 自增 id）
     */
    TradeOrder createOrder(OrderCreateDto dto);

    /**
     * 支付订单（★ 同时更新 MySQL + ES 的状态）
     */
    void payOrder(Long id);

    /**
     * 对比查询：分别查 MySQL 和 ES，看两边数据是否一致
     *
     * @return { mysql: [...], es: [...], mysqlCount: n, esCount: n }
     */
    Map<String, Object> compareData();

    /**
     * 只查 MySQL（模拟「详情走 MySQL」）
     */
    List<TradeOrder> listFromMysql();

    /**
     * 只查 ES（模拟「搜索走 ES」）
     */
    Map<String, Object> listFromEs(String keyword, Integer status);

    /**
     * 手动同步：把 MySQL 数据全量刷到 ES（对齐 qingo 的 DbEsSyncService）
     * 用于演示「数据不一致时怎么修正」
     *
     * @return 同步了多少条
     */
    int syncMysqlToEs();

    /**
     * 删除订单（★ 同时删 MySQL + ES）
     */
    void deleteOrder(Long id, String esDocId);
}
