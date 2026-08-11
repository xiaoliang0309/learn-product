package com.overseas.learning.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建商户 DTO
 *
 * @NotBlank = 字符串不能为 null 且不能为空
 * @NotNull  = 对象不能为 null
 * @Email    = 邮箱格式校验
 */
@Data
public class MerchantCreateDto {

    @NotBlank(message = "商户名称不能为空")
    private String fullName;

    private String shortName;

    @NotNull(message = "收款方式不能为空")
    private Integer bizType;  // 1-自收 2-代收

    private String currency;

    @NotBlank(message = "国家不能为空")
    private String country;

    private String state;
    private String city;
    private String address;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
}