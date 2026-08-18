package com.overseas.learning.common;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentUser 参数解析器 —— 对齐 qingo 的 AuthUserMethodArgumentResolver
 *
 * 【它是干嘛的？】
 *   让 Controller 的方法参数能直接用 @CurrentUser 拿到当前用户。
 *   没有它，你写 @CurrentUser LoginUser user，Spring 不知道这个参数从哪来。
 *
 * 【工作原理（两步）】
 *   1. supportsParameter：判断「这个方法参数是不是带 @CurrentUser、且类型是 LoginUser」
 *      → 是的话，才交给 resolveArgument 处理
 *   2. resolveArgument：真正「解析出参数值」的地方
 *      → 从 request 里取出拦截器存好的 LoginUser，返回给 Controller
 *
 * 【整条链路】
 *   拦截器 preHandle 存：request.setAttribute("loginUser", user)
 *        ↓
 *   Controller 方法参数写：@CurrentUser LoginUser user
 *        ↓
 *   本解析器 resolveArgument 取：request.getAttribute("loginUser") → 塞进 user
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断：这个参数要不要由我来解析？
     * 规则：带 @CurrentUser 注解，且类型是 LoginUser
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().isAssignableFrom(LoginUser.class);
    }

    /**
     * 真正解析：从 request 里取出拦截器存的 LoginUser
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 从 request 作用域取出拦截器存的 user（key 要和拦截器里一致）
        return webRequest.getAttribute(AuthInterceptor.LOGIN_USER_KEY, RequestAttributes.SCOPE_REQUEST);
    }
}
