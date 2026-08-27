package com.overseas.learning.service.impl;

import com.overseas.learning.dto.DocEsOrderSearchRequest;
import com.overseas.learning.entity.DocEsOrder;
import com.overseas.learning.service.DocEsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ES 订单 Service 实现 —— 对齐 qingo 的 DocEsMctOrderServiceImpl
 *
 * 【对齐 qingo】
 *   qingo 的核心写法（DocEsMctOrderServiceImpl）：
 *
 *     @Resource
 *     private ElasticsearchRestTemplate elasticsearchRestTemplate;
 *
 *     // 写入
 *     public void save(DocEsMctOrder doc) {
 *         elasticsearchRestTemplate.save(doc);
 *     }
 *
 *     // 按ID查
 *     public DocEsMctOrder getById(String id) {
 *         return elasticsearchRestTemplate.get(id, DocEsMctOrder.class);
 *     }
 *
 *     // 删除
 *     public void deleted(String id) {
 *         elasticsearchRestTemplate.delete(id, DocEsMctOrder.class);
 *     }
 *
 *     // 条件查询（★ 重点）
 *     public Page<String> getList(request, page, size) {
 *         BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(request);  // 构建条件
 *         NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
 *                 .withQuery(boolQueryBuilder)                               // 查询条件
 *                 .withSorts(new FieldSortBuilder("create_time")              // 排序
 *                         .order(SortOrder.DESC))
 *                 .withPageable(PageRequest.of(page - 1, size))              // 分页
 *                 .withTrackTotalHits(true)                                   // 返回总数
 *                 .build();
 *         SearchHits<DocEsMctOrder> hits = elasticsearchRestTemplate
 *                 .search(searchQuery, DocEsMctOrder.class);
 *         List<String> ids = hits.stream()
 *                 .map(SearchHit::getContent)
 *                 .map(DocEsMctOrder::getId)
 *                 .collect(Collectors.toList());
 *         return new Page<>(hits.getTotalHits(), ids);
 *     }
 *
 *     // 聚合统计
 *     TermsAggregationBuilder agg = AggregationBuilders.terms("status")
 *             .field("status").size(10);
 *     queryBuilder.withAggregations(agg);
 *
 * 【关键对象】
 *   ElasticsearchRestTemplate：Spring 操作 ES 的工具，就像 MongoTemplate 之于 MongoDB
 *   BoolQueryBuilder：组合多个条件（filter/must/should/mustNot），相当于 SQL 的 WHERE AND/OR
 *   NativeSearchQuery：组装好的查询对象（条件 + 排序 + 分页 + 聚合）
 *   SearchHits<T>：查询结果，包含命中的文档和总数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocEsOrderServiceImpl implements DocEsOrderService {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void save(DocEsOrder doc) {
        elasticsearchRestTemplate.save(doc);
        log.info("【ES】写入订单: id={}, mctName={}, status={}", doc.getId(), doc.getMctName(), doc.getStatus());
    }

    @Override
    public DocEsOrder getById(String id) {
        DocEsOrder doc = elasticsearchRestTemplate.get(id, DocEsOrder.class);
        log.info("【ES】按ID查询: id={}, 结果={}", id, doc != null ? "有" : "无");
        return doc;
    }

    @Override
    public void delete(String id) {
        elasticsearchRestTemplate.delete(id, DocEsOrder.class);
        log.info("【ES】删除订单: id={}", id);
    }

    @Override
    public void batchSave(List<DocEsOrder> docs) {
        elasticsearchRestTemplate.save(docs);
        log.info("【ES】批量写入订单: 条数={}", docs.size());
    }

    @Override
    public void deleteAll() {
        // 删除整个索引（学习用，重置数据）
        try {
            elasticsearchRestTemplate.indexOps(DocEsOrder.class).delete();
            elasticsearchRestTemplate.indexOps(DocEsOrder.class).create();
            log.info("【ES】已清空并重建索引 doc_order");
        } catch (Exception e) {
            log.error("【ES】清空索引失败", e);
        }
    }

    @Override
    public Map<String, Object> search(DocEsOrderSearchRequest request, int page, int size) {
        // 1. 构建查询条件（BoolQueryBuilder = WHERE + AND/OR）
        BoolQueryBuilder boolQueryBuilder = buildQuery(request);

        // 2. 组装查询（条件 + 排序 + 分页）
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSorts(new FieldSortBuilder("create_time").order(SortOrder.DESC))
                .withPageable(PageRequest.of(page - 1, size))
                .withTrackTotalHits(true)
                .build();

        // 3. 执行查询
        SearchHits<DocEsOrder> searchHits = elasticsearchRestTemplate.search(searchQuery, DocEsOrder.class);

        // 4. 提取结果
        List<DocEsOrder> list = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", searchHits.getTotalHits());
        result.put("list", list);
        log.info("【ES】搜索: request={}, page={}, size={}, 命中={}",
                request, page, size, searchHits.getTotalHits());
        return result;
    }

    @Override
    public Map<String, Long> countByStatus(Long mctId) {
        // 1. 构建过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (mctId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mct_id", mctId));
        }

        // 2. 聚合：按 status 分组统计（对齐 qingo 的 getGroupCount）
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms("status").field("status").size(10);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(boolQueryBuilder);
        queryBuilder.withAggregations(aggregationBuilder);
        NativeSearchQuery nativeSearchQuery = queryBuilder.build();

        SearchHits<DocEsOrder> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, DocEsOrder.class);

        // 3. 解析聚合结果
        Map<String, Long> result = new HashMap<>();
        if (searchHits.getAggregations() != null) {
            // Spring Data ES 4.4 的 aggregations() 返回原始聚合对象
            // 对齐 qingo 的解析方式：遍历聚合结果，找名为 "status" 的 terms 聚合
            @SuppressWarnings("unchecked")
            Iterable<org.elasticsearch.search.aggregations.Aggregation> aggList =
                    (Iterable<org.elasticsearch.search.aggregations.Aggregation>) searchHits.getAggregations().aggregations();
            if (aggList != null) {
                aggList.forEach(agg -> {
                    if ("status".equals(agg.getName()) && agg instanceof org.elasticsearch.search.aggregations.bucket.terms.Terms) {
                        ((org.elasticsearch.search.aggregations.bucket.terms.Terms) agg).getBuckets().forEach(bucket -> {
                            String statusName;
                            if (Integer.valueOf(0).equals(bucket.getKey())) {
                                statusName = "pending_pay";
                            } else if (Integer.valueOf(1).equals(bucket.getKey())) {
                                statusName = "paid";
                            } else if (Integer.valueOf(2).equals(bucket.getKey())) {
                                statusName = "refunded";
                            } else {
                                statusName = String.valueOf(bucket.getKey());
                            }
                            result.put(statusName, bucket.getDocCount());
                        });
                    }
                });
            }
        }
        log.info("【ES】状态统计: mctId={}, result={}", mctId, result);
        return result;
    }

    /**
     * 构建查询条件 —— 对齐 qingo 的 getBoolQueryBuilder
     *
     * qingo 的写法（核心模式）：
     *   BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
     *   if (request.getStatus() != null) {
     *       boolQueryBuilder.filter(QueryBuilders.termQuery("status", request.getStatus()));
     *   }
     *   if (StringUtils.isNotBlank(request.getGoodsName())) {
     *       boolQueryBuilder.filter(QueryBuilders.wildcardQuery("goods_codes", "*" + name + "*"));
     *   }
     *
     * filter vs must：
     *   filter → 只过滤，不算相关性得分（精确匹配/范围查询用，性能更好）
     *   must   → 算相关性得分（全文搜索用）
     *   qingo 绝大多数用 filter（因为是业务精确查询，不需要相关性得分）
     *
     * 【对象传条件】request 里的字段为 null 就跳过（不加这个条件），
     *   和 MyBatis 的 <if test="字段 != null"> 一个道理。
     */
    private BoolQueryBuilder buildQuery(DocEsOrderSearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (request == null) {
            return boolQueryBuilder;
        }

        // 精确匹配：状态
        if (request.getStatus() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("status", request.getStatus()));
        }

        // 精确匹配：商户ID
        if (request.getMctId() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mct_id", request.getMctId()));
        }

        // 精确匹配：店铺编码
        if (request.getShopCode() != null && !request.getShopCode().trim().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("shop_code", request.getShopCode().trim()));
        }

        // 模糊搜索：商品名称（wildcard = SQL 的 LIKE）
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.wildcardQuery("goods_names_text", "*" + request.getKeyword().trim() + "*"));
        }

        return boolQueryBuilder;
    }
}
