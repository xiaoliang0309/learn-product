package com.overseas.learning.dto;

import lombok.Data;

/**
 * 商户查询 DTO — 包装分页 + 筛选条件
 *
 * 与项目中的查询 DTO 模式一致（MctLedgerQueryDto 等）
 */
@Data
public class MerchantQueryDto {

    /** 页码，默认第 1 页 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;

    /** 商户名称（模糊搜索） */
    private String fullName;

    /** 收款方式 */
    private Integer bizType;

    /** 状态 */
    private Integer status;
}