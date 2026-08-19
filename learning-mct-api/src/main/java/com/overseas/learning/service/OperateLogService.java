package com.overseas.learning.service;

import com.overseas.learning.entity.OperateLogDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 操作日志 Service —— 把「操作流水」写到 MongoDB
 *
 * 【典型用法】
 *   业务操作（增删改商户）成功后，调 log() 记一条日志。
 *   业务数据在 MySQL，操作日志在 MongoDB，两边各记一笔。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperateLogService {

    private final MongoTemplate mongoTemplate;

    /**
     * 记一条操作日志
     *
     * @param targetType  操作对象（merchant/order...）
     * @param targetId    对象ID
     * @param operateType 操作类型（create/update/delete）
     * @param content     操作内容描述
     * @param operator    操作人
     */
    public void log(String targetType, Long targetId, String operateType, String content, String operator) {
        OperateLogDoc doc = OperateLogDoc.builder()
                .targetType(targetType)
                .targetId(targetId)
                .operateType(operateType)
                .content(content)
                .operator(operator)
                .operateTime(new Date())
                .build();
        mongoTemplate.save(doc);
        log.info("【MongoDB-操作日志】{} {} id={}: {}", targetType, operateType, targetId, content);
    }

    /**
     * 查询某对象的操作日志（按时间倒序）
     */
    public List<OperateLogDoc> listByTarget(String targetType, Long targetId) {
        Query query = Query.query(Criteria.where("target_type").is(targetType).and("target_id").is(targetId));
        List<OperateLogDoc> list = mongoTemplate.find(query, OperateLogDoc.class);
        log.info("【MongoDB-操作日志】查询 {} id={}, 共 {} 条", targetType, targetId, list.size());
        return list;
    }

    /**
     * 查询全部操作日志（演示用）
     */
    public List<OperateLogDoc> listAll() {
        return mongoTemplate.findAll(OperateLogDoc.class);
    }
}
