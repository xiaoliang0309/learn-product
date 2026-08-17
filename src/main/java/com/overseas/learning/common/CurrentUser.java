package com.overseas.learning.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @CurrentUser 注解 —— 与 qingo 的 CurrentUser 完全一致
 *
 * 用法：在 Controller 方法参数上加它，就能直接拿到当前登录用户：
 *
 *   @GetMapping("/info")
 *   public Result info(@CurrentUser LoginUser user) {
 *       // user 就是当前登录用户，不用自己查
 *   }
 *
 * 原理：配合 CurrentUserArgumentResolver（参数解析器），
 *   Spring 调用 Controller 方法前，发现参数带 @CurrentUser，
 *   就去 request 里取出拦截器存好的 LoginUser，塞进这个参数。
 */
@Target(ElementType.PARAMETER)   // 只能加在「方法参数」上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
