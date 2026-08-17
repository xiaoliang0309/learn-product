package com.overseas.learning.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户 —— 对应 qingo 的 AuthWorker
 *
 * 拦截器解析 token 后，把用户信息装进这个对象，
 * 存到 request 里，Controller 用 @CurrentUser 直接拿到。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户ID */
    private Long uid;

    /** 用户名 */
    private String username;

    /** 所属商户ID */
    private Long merchantId;
}
