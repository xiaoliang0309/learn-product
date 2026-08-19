package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 设备日志文档 —— 对应 MongoDB 的 doc_device_log 集合
 *
 * 【MongoDB 和 MySQL 的区别（看懂这个就够了）】
 *   MySQL：先建表（固定字段），存的是「行」，用 SQL 查
 *   MongoDB：不用建表，存的是「文档」(JSON)，字段可以灵活变化
 *
 * 【注解对照（对齐 qingo 的 DocDeviceTest）】
 *   @Document(collection = "doc_device_log")  ≈ MySQL 的 @Table(表名)
 *     → 告诉 MongoDB 这个类对应哪个「集合」(collection，相当于表)
 *   @Field("xxx")  ≈ MySQL 的 @Column(字段名)
 *     → 告诉 MongoDB 这个属性对应文档里的哪个字段
 *
 * 【为什么设备/日志数据用 MongoDB】
 *   qingo 里设备上报数据、操作日志这类「字段不固定、量大、写入频繁」的数据用 Mongo，
 *   因为它比 MySQL 更灵活（不用改表结构）、写入快。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doc_device_log")   // 对应 MongoDB 的集合（≈表）
public class DeviceLogDoc {

    @Field("_id")           // MongoDB 的主键固定叫 _id
    private String id;

    @Field("device_code")   // 设备编码
    private String deviceCode;

    @Field("action")        // 操作类型（开门/关门/自检...）
    private String action;

    @Field("content")       // 日志内容（JSON 字符串）
    private String content;

    @Field("create_time")   // 创建时间
    private Date createTime;
}
