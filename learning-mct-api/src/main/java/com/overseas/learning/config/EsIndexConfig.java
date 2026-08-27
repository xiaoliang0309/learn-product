package com.overseas.learning.config;

import com.overseas.learning.entity.DocEsOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * ES 索引初始化配置
 *
 * 【作用】
 *   应用启动时，确保 doc_order 索引存在且 mapping 正确。
 *   如果索引不存在或 mapping 不对，自动创建/重建。
 *
 * 【为什么需要】
 *   Spring Data ES 的 @Document(createIndex = true) 自动建索引时，
 *   Date 字段可能被推断为 text 而不是 date，导致排序/聚合报错。
 *   这里用 indexOps 显式创建，确保 mapping 正确。
 *
 * 【对齐 qingo】
 *   qingo 的索引可能由运维预先建好，或用类似方式初始化。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsIndexConfig {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 应用启动完成后执行：确保 ES 索引存在
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initIndex() {
        try {
            IndexOperations indexOps = elasticsearchRestTemplate.indexOps(DocEsOrder.class);
            // 检查索引是否存在
            if (!indexOps.exists()) {
                log.info("【ES】索引 doc_order 不存在，正在创建...");
                indexOps.create();
                indexOps.putMapping();
                log.info("【ES】索引 doc_order 创建成功，mapping 已写入");
            } else {
                log.info("【ES】索引 doc_order 已存在");
            }
        } catch (Exception e) {
            log.error("【ES】索引初始化失败（不影响其他功能）: {}", e.getMessage());
        }
    }
}
