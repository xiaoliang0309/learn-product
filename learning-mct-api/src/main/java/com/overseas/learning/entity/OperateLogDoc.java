package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 操作日志文档 —— 对应 MongoDB 的 doc_operate_log 集合
 *
 * 【这是干嘛的】
 *   记录「谁对什么数据做了什么操作」的日志，对齐 qingo 的 DocMctOrderOperateLog。
 *   MySQL 存业务数据，MongoDB 存这种「操作流水/日志」——这是 qingo 里 MongoDB 最典型的用途。
 *
 * 【为什么操作日志放 MongoDB 不放 MySQL】
 *   1. 日志量大、只增不改、字段可能变 → MongoDB 更灵活、写入快
 *   2. 不污染业务库（MySQL 只存正经业务数据）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doc_operate_log")
public class OperateLogDoc {

    @Field("_id")
    private String id;

    /** 操作对象类型：merchant=商户 / onboarding=进件 / order=订单 */
    @Field("target_type")
    private String targetType;

    /** 操作对象ID（比如商户ID） */
    @Field("target_id")
    private Long targetId;

    /** 操作类型：create=新增 / update=更新 / delete=删除 */
    @Field("operate_type")
    private String operateType;

    /** 操作内容（描述这次做了什么） */
    @Field("content")
    private String content;

    /** 操作人 */
    @Field("operator")
    private String operator;

    /** 操作时间 */
    @Field("operate_time")
    private Date operateTime;
}
