package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.common.annotation.WebLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AOP 操作日志演示 Controller —— 对齐 qingo 的 @WebLog + WebLogAspect
 *
 * 【演示什么？】
 *   你在下面这些接口里看不到任何「记开始时间/拼日志/算耗时」的代码，
 *   但访问它们时，WebLogAspect 切面会自动打印一段完整操作日志（带 reqId）。
 *   这就是 AOP「无侵入」：日志逻辑在方法外面包了一层，业务代码零改动。
 *
 * 【观察点】
 *   1. POST /api/aop/write（@WebLog）→ 控制台会出现「操作日志 @WebLog」+ reqId
 *   2. GET  /api/aop/query（不加注解的 GET）→ 默认不切，不打印（GET 查询量太大）
 *   3. GET  /api/aop/queryLog（GET 但加了 @WebLog）→ 也会打印
 *   4. 看每条日志前面的 [reqId]：同一次请求里 Controller/Biz 的日志 reqId 相同
 */
@Slf4j
@RestController
@RequestMapping("/api/aop")
public class AopDemoController {

    /**
     * POST /api/aop/write?name=xxx
     * 写操作（@PostMapping），天然在切面切点内 → 自动记操作日志
     * 再加个 @WebLog 是为了语义更明显（qingo 很多写接口都这么标）
     */
    @WebLog
    @PostMapping("/write")
    public Result<String> write(@RequestParam(defaultValue = "测试商品") String name) {
        // 注意：这里没有任何日志代码，但切面会自动记「类名.方法/参数/URL/耗时/结果」
        log.info("【业务】正在处理 write 业务逻辑（这行业务日志也带同一个 reqId）: name={}", name);
        return Result.success("写入成功: " + name);
    }

    /**
     * GET /api/aop/query —— 普通 GET，不加注解
     * 切面默认跳过（不切）→ 控制台不会打印操作日志
     */
    @GetMapping("/query")
    public Result<String> query() {
        log.info("【业务】query 查询（GET 且没加 @WebLog，默认不被切面记录）");
        return Result.success("查询成功（这条不会触发 @WebLog 切面日志）");
    }

    /**
     * GET /api/aop/queryLog —— GET 但加了 @WebLog
     * 虽然是 GET，但加了注解 → 切面也会记录
     */
    @WebLog
    @GetMapping("/queryLog")
    public Result<String> queryLog() {
        log.info("【业务】queryLog 查询（GET 但加了 @WebLog，会被切面记录）");
        return Result.success("查询成功（这条 GET 因加了 @WebLog 而被切面记录）");
    }
}
