package com.overseas.learning.service;

import com.overseas.learning.entity.DeviceLogDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 设备日志 Service —— 演示 MongoDB 的读写（对齐 qingo 的 mongoTemplate 用法）
 *
 * 【核心对象 MongoTemplate】
 *   就像 MyBatis 的 Mapper 之于 MySQL，MongoTemplate 是 Spring 操作 MongoDB 的工具。
 *   qingo 里大量用它的这些方法：
 *     mongoTemplate.save(obj)        → 保存（新增/更新）
 *     mongoTemplate.findById(id, X.class)  → 按 id 查
 *     mongoTemplate.find(query, X.class)   → 条件查
 *
 * 【日志写法】
 *   就是普通的 log.info(...)，和别的代码一样。
 *   日志里看到「insert into doc_device_log」「find ... doc_device_log」就是 MongoDB 操作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceLogService {

    private final MongoTemplate mongoTemplate;

    /**
     * 写一条设备日志到 MongoDB
     * 对应 qingo 的 mongoTemplate.save(...)
     */
    public DeviceLogDoc saveLog(String deviceCode, String action, String content) {
        DeviceLogDoc doc = DeviceLogDoc.builder()
                .deviceCode(deviceCode)
                .action(action)
                .content(content)
                .createTime(new Date())
                .build();

        // ★ MongoDB 写入：save（没有表，直接往集合里插一条 JSON 文档）
        DeviceLogDoc saved = mongoTemplate.save(doc);
        log.info("【MongoDB】写入设备日志: id={}, deviceCode={}, action={}", saved.getId(), deviceCode, action);
        return saved;
    }

    /**
     * 按 ID 查询
     * 对应 qingo 的 mongoTemplate.findById(...)
     */
    public DeviceLogDoc getById(String id) {
        DeviceLogDoc doc = mongoTemplate.findById(id, DeviceLogDoc.class);
        log.info("【MongoDB】按ID查询: id={}, 结果={}", id, doc != null ? "有" : "无");
        return doc;
    }

    /**
     * 按设备编码查日志列表（条件查询）
     * 对应 qingo 的 mongoTemplate.find(query, ...)
     */
    public List<DeviceLogDoc> listByDevice(String deviceCode) {
        // Query + Criteria 就是 MongoDB 的「条件」，相当于 MySQL 的 WHERE device_code = ?
        Query query = Query.query(Criteria.where("device_code").is(deviceCode));
        List<DeviceLogDoc> list = mongoTemplate.find(query, DeviceLogDoc.class);
        log.info("【MongoDB】按设备查询: deviceCode={}, 条数={}", deviceCode, list.size());
        return list;
    }
}
