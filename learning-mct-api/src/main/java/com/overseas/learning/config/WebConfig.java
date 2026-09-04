package com.overseas.learning.config;

import com.overseas.learning.common.AuthInterceptor;
import com.overseas.learning.common.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置 —— 注册拦截器和参数解析器
 * 对齐 qingo 的 WebMvcAutoConfiguration
 *
 * 【为什么需要这个配置类？】
 *   拦截器和参数解析器写好后，Spring 不知道要用它们。
 *   必须在这里「注册」一遍，告诉 Spring：
 *     - 哪些请求要经过 AuthInterceptor（拦截哪些路径）
 *     - 遇到 @CurrentUser 参数时用 CurrentUserArgumentResolver 解析
 *
 * 【WebMvcConfigurer】
 *   Spring 提供的「Web MVC 配置接口」，实现它就能定制：
 *     addInterceptors    → 注册拦截器
 *     addArgumentResolvers → 注册参数解析器
 *     （还能配跨域 addCorsMappings、静态资源等）
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 注册拦截器
     *
     * addPathPatterns("/**")      → 拦截所有请求
     * excludePathPatterns(...)    → 但排除这些（登录接口、前端静态页等不需要登录就能访问的）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")          // 只拦 /api 下的接口
                .excludePathPatterns(
                        "/api/merchants",             // 商户接口暂不拦（方便你测试）
                        "/api/onboarding/**",         // 进件接口暂不拦
                        "/api/orders",                // 订单接口暂不拦（Kafka 演示用）
                        "/merchant/**",               // Feign 演示接口暂不拦（路径是 /merchant/remote/{id}）
                        "/operate-log/**",            // MongoDB 操作日志演示接口暂不拦
                        "/api/aop/**"                 // AOP 操作日志演示接口暂不拦（方便测 @WebLog）
                );
        // 说明：学习项目为了不挡你前面的测试，只拦「需要登录」的演示接口。
        // qingo 真实项目是 addPathPatterns("/**") 几乎全拦，只放行登录/注册等少数接口。
    }

    /**
     * 注册参数解析器
     * 让 Controller 能用 @CurrentUser LoginUser 直接拿到当前用户
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
