package com.overseas.learning.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.common.annotation.WebLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 操作日志切面 —— 对齐 qingo 的 WebLogAspect
 *
 * 【AOP 三要素（对照记）】
 *   @Aspect          → 声明这是个切面
 *   @Pointcut        → 切点：在「哪些方法」上生效（这里是所有写操作接口 + @WebLog）
 *   @Around          → 通知：在切点方法「前后」干什么（这里记日志）
 *
 * 【执行流程】
 *   请求 → 代理的 doAround()
 *     【前】记开始时间、MDC 塞 reqId
 *     → joinPoint.proceed() 调真正的业务方法（业务里无一句日志）
 *     【后】算耗时、拼「类名.方法名/参数/URL/耗时/结果/异常」打印
 *
 * 【MDC reqId】
 *   MDC.put("reqId", ...) 把本次请求 ID 塞进日志上下文，
 *   之后这条线程打印的所有日志（Controller/Biz/数据层）都自动带同一个 reqId，
 *   排障时按 reqId 一搜就是一次请求的完整链路。logback pattern 里用 %X{reqId} 输出。
 */
@Aspect
@Order(-1)      // 多个切面时尽量先执行
@Component
@Slf4j
public class WebLogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 切点：所有 @PostMapping/@PutMapping/@DeleteMapping（写操作）+ 所有 @WebLog 注解的方法
     * GET 查询默认不记（量太大），除非特意加 @WebLog
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(com.overseas.learning.common.annotation.WebLog)")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;

        // GET 且没加 @WebLog → 直接放过，不记日志
        if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            if (!method.isAnnotationPresent(WebLog.class)) {
                return joinPoint.proceed();
            }
        }

        long startTime = System.currentTimeMillis();
        // 生成并塞入 reqId（链路追踪的关键：本次请求所有日志都带它）
        String reqId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        MDC.put("reqId", reqId);

        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();   // ★ 调用真正的业务方法
            return result;
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            long spendTime = System.currentTimeMillis() - startTime;
            try {
                String classMethod = joinPoint.getTarget().getClass().getSimpleName()
                        + "." + joinPoint.getSignature().getName() + "()";
                String argsJson = Arrays.stream(joinPoint.getArgs())
                        .map(a -> a == null ? "null" : a.getClass().getSimpleName())
                        .collect(Collectors.joining(", ", "[", "]"));

                StringBuilder sb = new StringBuilder("\n");
                sb.append("==================== 【操作日志 @WebLog】====================\n");
                sb.append("reqId        : ").append(reqId).append("\n");
                sb.append("Class Method : ").append(classMethod).append("\n");
                sb.append("Args         : ").append(argsJson).append("\n");
                if (request != null) {
                    sb.append("Request URL  : [").append(request.getMethod()).append("] ")
                            .append(request.getRequestURL()).append("\n");
                }
                if (throwable == null) {
                    sb.append("Response     : [").append(spendTime).append("ms] ")
                            .append(toJson(result)).append("\n");
                } else {
                    sb.append("Exception    : ").append(throwable.getClass().getSimpleName())
                            .append(" - ").append(throwable.getMessage()).append("\n");
                }
                if (spendTime > 1000) {
                    sb.append("Attention    : ⚠️ 慢请求（超过 1 秒）\n");
                }
                sb.append("==========================================================");
                log.info(sb.toString());
            } finally {
                MDC.remove("reqId");   // 用完清理，防线程复用残留（对齐 qingo 的 MDC.clear()）
            }
        }
    }

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
