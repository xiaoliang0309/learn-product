package com.overseas.learning.service;

import com.overseas.learning.dto.DocEsOrderSearchRequest;
import com.overseas.learning.entity.DocEsOrder;

import java.util.List;
import java.util.Map;

/**
 * ES 订单 Service —— 对齐 qingo 的 DocEsMctOrderService
 *
 * 【qingo 的接口定义】
 *   public interface DocEsMctOrderService {
 *       DocEsMctOrder getById(String id);
 *       void deleted(String id);
 *       void save(DocEsMctOrder doc);
 *       long getCount(DocMctOrderSearchRequest request);
 *       Page<String> getList(request, page, size);
 *   }
 *
 * 学习项目简化：保留 save/getById/delete/list/count + 分页搜索 + 聚合统计
 */
public interface DocEsOrderService {

    /** 保存/更新一条订单文档 */
    void save(DocEsOrder doc);

    /** 按 ID 查 */
    DocEsOrder getById(String id);

    /** 按 ID 删 */
    void delete(String id);

    /**
     * 分页搜索订单（★ 对齐 qingo：用 request 对象传条件）
     *
     * @param request 查询条件（字段为 null 则不加该条件）
     * @param page    页码（从 1 开始）
     * @param size    每页条数
     */
    Map<String, Object> search(DocEsOrderSearchRequest request, int page, int size);

    /** 统计各状态的订单数量（聚合查询，对齐 qingo 的 getGroupCount） */
    Map<String, Long> countByStatus(Long mctId);

    /** 批量写入（对齐 qingo 的 batchSave） */
    void batchSave(List<DocEsOrder> docs);

    /** 清空索引（学习用：重置数据） */
    void deleteAll();
}
