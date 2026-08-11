package com.overseas.learning.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 商户实体 — 对应 merchant 表
 *
 * 命名规范:
 *   表名: snake_case      → merchant
 *   字段: snake_case      → full_name, biz_type
 *   实体: PascalCase      → Merchant
 *   属性: camelCase       → fullName, bizType
 *  MyBatis 驼峰映射开启后自动转换，无需额外配置
 *
 * 注解说明（★ 与 qingo 真实项目一致）:
 *   @Data                — 自动生成 getter/setter/toString
 *   @Builder             — 支持 XxxEntity.builder().field(v).build() 链式构建（qingo 常用）
 *   @NoArgsConstructor   — 无参构造，MyBatis 反射创建对象必须用
 *   @AllArgsConstructor  — 全参构造，@Builder 内部需要
 *
 * 注意: @Builder 必须搭配 @AllArgsConstructor + @NoArgsConstructor，
 *       否则要么编译报错，要么 MyBatis 查库时报无参构造不存在。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    private Long id;
    private String fullName;
    private String shortName;

    /** 收款方式: 1-自收 2-代收 */
    private Integer bizType;

    private String currency;
    private String country;
    private String state;
    private String city;
    private String address;
    private String email;
    private String phone;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    private Date createdAt;
    private Date updatedAt;
}