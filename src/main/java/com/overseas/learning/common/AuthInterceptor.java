package com.overseas.learning.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器 —— 对齐 qingo 的 AuthInterceptor
 *
 * 【拦截器是干嘛的？】
 *   每个请求到达 Controller 之「前」，先经过这里。
 *   作用：解析请求头里的 token，查出当前登录用户，存到 request 里。
 *   这样 Controller 就不用每个都自己解析 token 了。
 *
 * 【执行时机】
 *   浏览器请求 → 【拦截器 preHandle】→ Controller 方法
 *                ↑ 在这里校验登录、解析用户
 *
 * 【qingo 的真实逻辑】
 *   1. 从请求头取 token（Auth-Token）
 *   2. 解析 token 得到用户（qingo 用 JWT）
 *   3. 解析失败 → 返回未授权，拦截（return false，不再进 Controller）
 *   4. 解析成功 → 把用户存进 request，放行（return true）
 *
 * 【学习项目简化】
 *   不搞真的 JWT，用一个「写死的假 token」演示流程。
 *   你只要理解「拦截器在 Controller 之前统一处理登录」这个思想即可。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** 存进 request 的 key，参数解析器靠它取用户 */
    public static final String LOGIN_USER_KEY = "loginUser";

    /**
     * 请求进入 Controller 之前执行
     *
     * @return true=放行（进 Controller）；false=拦截（不进，直接返回）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 0. 特殊放行：如果 URL 里 type=1，跳过登录校验（演示「条件跳过」用）
        //    request.getParameter 能拿到 URL 查询参数 ?type=1
        String type = request.getParameter("type");
        if ("1".equals(type)) {
            log.info("【拦截器】检测到 type=1，跳过登录校验: {}", request.getRequestURI());
            return true; // 直接放行，不校验 token
        }

        // 1. 从请求头取 token（前端请求时带上 Auth-Token: xxx）
        String token = request.getHeader("Auth-Token");

        // 2. 解析 token → 得到当前用户（学习项目用假数据演示）
        LoginUser user = parseToken(token);

        if (user == null) {
            // 3. 解析失败 → 没登录 → 拦截，返回 401，不进 Controller
            log.warn("【拦截器】token 无效或为空，拦截请求: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
            return false; // 拦截
        }

        // 4. 解析成功 → 把用户存进 request，放行
        //    之后 Controller 用 @CurrentUser 就能取到这个 user
        request.setAttribute(LOGIN_USER_KEY, user);
        log.info("【拦截器】登录用户: uid={}, username={}, uri={}", user.getUid(), user.getUsername(), request.getRequestURI());
        return true; // 放行
    }

    /**
     * 解析 token（学习项目简化版：写死一个假用户）
     *
     * 规则：token 为 "test-token" 就当成登录成功，返回一个假用户；否则返回 null（未登录）。
     * 真实项目（qingo）这里是用 JWT 解析 token，取出用户信息。
     */
    private LoginUser parseToken(String token) {
        if ("test-token".equals(token)) {
            return LoginUser.builder()
                    .uid(1001L)
                    .username("admin")
                    .merchantId(1L)
                    .build();
        }
        return null;
    }
}
