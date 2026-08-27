package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * ES 订单文档实体 —— 对齐 qingo 的 DocEsMctOrder
 *
 * 【对齐 qingo】
 *   qingo 的写法（DocEsMctOrder.java）：
 *     @Document(indexName = "doc_mct_order", createIndex = true)
 *     public class DocEsMctOrder implements Serializable {
 *         @Id @Field(name = "id") private String id;
 *         @Field(name = "status", type = FieldType.Integer) private Integer status;
 *         ...
 *     }
 *
 * 【关键注解】
 *   @Document(indexName = "xxx", createIndex = true)
 *     indexName：ES 索引名（≈ MySQL 的表名），这里用 doc_order
 *     createIndex = true：启动时自动创建索引（不用手动建）
 *
 *   @Id：标记主键字段（ES 的 _id）
 *
 *   @Field(name = "xxx", type = FieldType.Xxx)
 *     name：ES 里存的字段名（蛇形，如 mct_id）
 *     type：字段类型，常用：
 *       Keyword  → 不分词，精确匹配（订单号、设备编码、状态码）
 *       Text     → 分词，全文搜索（商品名称）
 *       Integer/Long → 数值，范围查询
 *       Boolean  → 布尔
 *       Date     → 日期，支持范围查询
 *
 * 【与 MySQL 实体的区别】
 *   MySQL 实体用 @TableName，ES 实体用 @Document
 *   MySQL 字段不加 @Field，ES 每个字段都要加 @Field 指定类型
 *   ES 的 Keyword vs Text 是核心区别：Keyword 不分词（精确匹配），Text 分词（全文搜索）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "doc_order", createIndex = true)
public class DocEsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Field(name = "id")
    private String id;

    /** 订单类型：1正常 2异常（对齐 qingo 的 type 字段） */
    @Field(name = "type", type = FieldType.Integer)
    private Integer type;

    /** 订单状态：0待支付 1已支付 2已退款 */
    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    /** 商户ID */
    @Field(name = "mct_id", type = FieldType.Long)
    private Long mctId;

    /** 商户名称（Text 类型，支持全文搜索） */
    @Field(name = "mct_name", type = FieldType.Text)
    private String mctName;

    /** 店铺编码（Keyword 类型，精确匹配） */
    @Field(name = "shop_code", type = FieldType.Keyword)
    private String shopCode;

    /** 货柜编码（Keyword 类型，精确匹配） */
    @Field(name = "cabinet_code", type = FieldType.Keyword)
    private String cabinetCode;

    /** 商品名称文本（Text 类型，全文搜索） */
    @Field(name = "goods_names_text", type = FieldType.Text)
    private String goodsNamesText;

    /** 支付方式：1微信 2支付宝 */
    @Field(name = "pay_plat", type = FieldType.Integer)
    private Integer payPlat;

    /** 订单金额（分） */
    @Field(name = "amount", type = FieldType.Long)
    private Long amount;

    /** 创建时间（Date 类型，支持范围查询） */
    @Field(name = "create_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 支付时间 */
    @Field(name = "pay_time", type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;
}
