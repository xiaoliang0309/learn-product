package com.overseas.learning.biz.impl;

import com.overseas.learning.biz.BizOrderDualService;
import com.overseas.learning.dao.TradeOrderMapper;
import com.overseas.learning.dto.DocEsOrderSearchRequest;
import com.overseas.learning.dto.OrderCreateDto;
import com.overseas.learning.entity.DocEsOrder;
import com.overseas.learning.entity.TradeOrder;
import com.overseas.learning.service.DocEsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单双写业务实现 —— 演示「下单时同时写 MySQL + ES」
 *
 * 【★ 核心思想】
 *   MySQL 是「主数据」：保证业务正确（事务、精确查询）
 *   ES 是「搜索副本」：保证搜索快（倒排索引、模糊搜索、聚合统计）
 *   下单时同时写两份，搜索走 ES，详情走 MySQL。
 *
 * 【对齐 qingo】
 *   qingo 的 BizMctOrderServiceImpl.createOrder() 就是这个模式：
 *     1. orderMapper.insert(order)            → MySQL
 *     2. docEsMctOrderService.save(esDoc)     → ES（双写）
 *
 * 【数据一致性】
 *   双写可能不一致（比如 MySQL 成功但 ES 失败）。
 *   qingo 的解决办法：
 *     1. 双写时尽量保证（同事务 / 先 MySQL 后 ES）
 *     2. 定时任务（XXL-JOB）把 MySQL 全量刷到 ES 修正 → 本类的 syncMysqlToEs()
 *     3. 以 MySQL 为准，ES 只是副本，ES 挂了不影响下单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizOrderDualServiceImpl implements BizOrderDualService {

    private final TradeOrderMapper tradeOrderMapper;   // MySQL 数据层
    private final DocEsOrderService docEsOrderService;  // ES 数据层

    /**
     * 下单（★ 核心：同时写 MySQL + ES）
     *
     * @Transactional 只保证 MySQL 的事务（ES 不在事务里）。
     * qingo 也一样：ES 写入失败不回滚 MySQL（因为 ES 是副本，可后续补偿）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(OrderCreateDto dto) {
        // ════════════════════════════════════════════════════
        // 第 0 步：DTO → Entity 转换（★ 对齐 qingo，用 @Builder）
        //   这一步就是「把参数转换为实体类」：
        //   DTO 只带前端传的字段，实体补默认值（status/currency/createdAt）
        // ════════════════════════════════════════════════════
        TradeOrder order = TradeOrder.builder()
                .orderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .merchantId(dto.getMerchantId())
                .shopId(dto.getShopId())
                .amount(dto.getAmount())
                .receiveType(dto.getReceiveType() != null ? dto.getReceiveType() : 1)
                .paymentMethod(dto.getPaymentMethod())
                .currency("USD")
                .status(0)                                  // 默认待支付
                .createdAt(new Date())
                .build();

        // ════════════════════════════════════════════════════
        // 第 1 步：写 MySQL（主数据，@Transactional 保证）
        // ════════════════════════════════════════════════════
        tradeOrderMapper.insert(order);        // MySQL 自增 id 回填到 order.id
        log.info("【双写-MySQL】订单入库: id={}, orderNo={}, amount={}", order.getId(), order.getOrderNo(), order.getAmount());

        // ════════════════════════════════════════════════════
        // 第 2 步：同时写 ES（搜索副本）
        //   把 MySQL 实体转换成 ES 实体（字段名/类型对齐）
        //   ES 的 id 用 MySQL 的自增 id 转字符串，保证两边能对上
        // ════════════════════════════════════════════════════
        try {
            DocEsOrder esDoc = toEsDoc(order);
            docEsOrderService.save(esDoc);
            log.info("【双写-ES】订单写入 ES: esId={}, orderNo={}", esDoc.getId(), order.getOrderNo());
        } catch (Exception e) {
            // ES 写失败不影响下单（记录日志，后续用 syncMysqlToEs 补偿）
            log.error("【双写-ES】ES 写入失败（不影响下单，可后续补偿）: {}", e.getMessage());
        }

        return order;
    }

    /**
     * 支付订单（★ 同时更新 MySQL + ES 的状态为已支付）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id) {
        // 1. 更新 MySQL 状态
        tradeOrderMapper.updateStatus(id, 1);
        TradeOrder order = tradeOrderMapper.selectById(id);
        log.info("【双写-MySQL】订单支付: id={}, status=1", id);

        // 2. 同步更新 ES 状态
        try {
            DocEsOrder esDoc = toEsDoc(order);
            esDoc.setStatus(1);
            esDoc.setPayTime(new Date());
            docEsOrderService.save(esDoc);   // save 带同 id 就是覆盖更新
            log.info("【双写-ES】订单支付状态同步: esId={}", esDoc.getId());
        } catch (Exception e) {
            log.error("【双写-ES】支付状态同步失败: {}", e.getMessage());
        }
    }

    /**
     * 对比查询：分别查 MySQL 和 ES，看两边是否一致
     * 这是「数据一致性」最直观的演示
     */
    @Override
    public Map<String, Object> compareData() {
        // 查 MySQL
        List<TradeOrder> mysqlList = tradeOrderMapper.selectList(new TradeOrder());
        long mysqlCount = tradeOrderMapper.count(new TradeOrder());

        // 查 ES（传空 request = 不带条件，查全部）
        Map<String, Object> esResult = docEsOrderService.search(DocEsOrderSearchRequest.builder().build(), 1, 50);

        Map<String, Object> result = new HashMap<>();
        result.put("mysqlList", mysqlList);
        result.put("mysqlCount", mysqlCount);
        result.put("esList", esResult.get("list"));
        result.put("esCount", esResult.get("total"));
        log.info("【双写-对比】MySQL {} 条, ES {} 条", mysqlCount, esResult.get("total"));
        return result;
    }

    /**
     * 只查 MySQL（模拟「详情/管理后台走 MySQL」）
     */
    @Override
    public List<TradeOrder> listFromMysql() {
        return tradeOrderMapper.selectList(new TradeOrder());
    }

    /**
     * 只查 ES（模拟「搜索走 ES」，支持商品名模糊搜索）
     */
    @Override
    public Map<String, Object> listFromEs(String keyword, Integer status) {
        // 对齐 qingo：用 request 对象传条件，字段为 null 就跳过
        DocEsOrderSearchRequest request = DocEsOrderSearchRequest.builder()
                .keyword(keyword)
                .status(status)
                .build();
        return docEsOrderService.search(request, 1, 50);
    }

    /**
     * 手动同步：把 MySQL 数据全量刷到 ES
     *
     * 【对齐 qingo 的 DbEsSyncService】
     *   qingo 用 XXL-JOB 定时（比如每天凌晨）把 MySQL 数据同步到 ES，
     *   修正双写不一致的问题。这里演示手动触发版。
     */
    @Override
    public int syncMysqlToEs() {
        List<TradeOrder> mysqlList = tradeOrderMapper.selectList(new TradeOrder());
        int count = 0;
        for (TradeOrder order : mysqlList) {
            try {
                docEsOrderService.save(toEsDoc(order));
                count++;
            } catch (Exception e) {
                log.error("【同步】订单 id={} 同步失败: {}", order.getId(), e.getMessage());
            }
        }
        log.info("【同步】MySQL → ES 全量同步完成，共同步 {} 条", count);
        return count;
    }

    /**
     * 删除订单（★ 同时删 MySQL + ES）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id, String esDocId) {
        tradeOrderMapper.deleteById(id);
        log.info("【双写-MySQL】删除订单: id={}", id);
        try {
            docEsOrderService.delete(esDocId);
            log.info("【双写-ES】删除 ES 文档: esId={}", esDocId);
        } catch (Exception e) {
            log.error("【双写-ES】删除失败: {}", e.getMessage());
        }
    }

    /**
     * MySQL 实体 → ES 实体 转换（★ 双写的关键：字段对齐）
     *
     * ES 的 id 用 MySQL 自增 id 转字符串，保证两边唯一标识一致。
     * 这样「按 id 删 ES」「按 id 更新 ES」都能对上 MySQL 的记录。
     */
    private DocEsOrder toEsDoc(TradeOrder order) {
        return DocEsOrder.builder()
                .id(String.valueOf(order.getId()))          // ★ ES id = MySQL id（对齐）
                .type(1)
                .status(order.getStatus())
                .mctId(order.getMerchantId())
                .mctName("商户" + order.getMerchantId())       // 演示：实际应 join 商户表查名称
                .shopCode(order.getShopId() != null ? "SHOP" + order.getShopId() : null)
                .cabinetCode(null)
                .goodsNamesText(order.getOrderNo())          // 演示：用订单号当搜索关键词
                .payPlat("WECHAT".equals(order.getPaymentMethod()) ? 1 : 2)
                .amount(order.getAmount())
                .createTime(order.getCreatedAt() != null ? order.getCreatedAt() : new Date())
                .payTime(order.getStatus() >= 1 ? new Date() : null)
                .build();
    }
}
