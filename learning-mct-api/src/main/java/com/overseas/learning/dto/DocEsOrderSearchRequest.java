package com.overseas.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ES 订单查询条件 DTO —— 对齐 qingo 的 DocMctOrderSearchRequest
 *
 * 【为什么用对象传条件，不用一堆参数】
 *   qingo 的 ES 查询方法签名是这样的：
 *     Page<String> getList(DocMctOrderSearchRequest request, Integer page, Integer size);
 *   所有查询条件都装在 request 对象里，字段为 null 就不加这个条件。
 *
 *   好处（和 MyBatis 传对象一个道理）：
 *     1. 查询条件再多，方法签名不变（就传一个 request）
 *     2. 字段为 null 自动跳过（动态拼条件）
 *     3. 同一个方法既能查全部（传空对象），也能按条件查（传带值的对象）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocEsOrderSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品名/关键词（模糊搜索） */
    private String keyword;

    /** 订单状态：0待支付 1已支付 2已退款 */
    private Integer status;

    /** 商户ID（精确匹配） */
    private Long mctId;

    /** 店铺编码（精确匹配） */
    private String shopCode;
}
