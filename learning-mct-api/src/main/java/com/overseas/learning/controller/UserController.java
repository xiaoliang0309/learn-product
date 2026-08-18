package com.overseas.learning.controller;

import com.overseas.learning.common.CurrentUser;
import com.overseas.learning.common.LoginUser;
import com.overseas.learning.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户演示 Controller —— 演示 @CurrentUser 获取当前登录用户
 *
 * 【这个接口演示什么？】
 *   看 info() 方法的参数：@CurrentUser LoginUser user
 *   你不用自己解析 token、不用查数据库，直接就能拿到当前登录用户。
 *   这就是 qingo 里满屏 @CurrentUser AuthWorker 的实现原理。
 *
 * 【测试方法】
 *   1. 不带 token 访问 → 被拦截器拦下，返回 401
 *      curl http://localhost:9090/api/user/info
 *   2. 带上正确 token 访问 → 拦截器放行，@CurrentUser 注入用户
 *      curl -H "Auth-Token: test-token" http://localhost:9090/api/user/info
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * GET /api/user/info — 获取当前登录用户信息
     *
     * 重点看参数：@CurrentUser LoginUser user
     *   - 拦截器先解析 token 得到用户，存进 request
     *   - 参数解析器从 request 取出，塞进这个 user 参数
     *   - 你直接用，一行解析代码都不用写
     */
    @GetMapping("/info")
    public Result<LoginUser> info(@CurrentUser LoginUser user) {
        log.info("【@CurrentUser演示】Controller 拿到的当前用户: {}", user);
        return Result.success(user);
    }

    /**
     * GET /api/user/info2?type=1 — 演示「接收参数 + 条件跳过校验」
     *
     * 【怎么接收前端参数？】
     *   @RequestParam 用来接收 URL 查询参数（?type=1 里的 type）。
     *   required = false 表示这个参数可传可不传；defaultValue 指定默认。
     *
     * 【业务规则】
     *   type = 1 → 跳过登录校验（不需要 token 也能访问）
     *   type 其他/不传 → 正常走 @CurrentUser，需要 token
     *
     * 注意：要让 type=1 能跳过登录，得在拦截器里也判断 type（见 AuthInterceptor）。
     */
    @GetMapping("/info2")
    public Result<Object> info2(@RequestParam(required = false) Integer type,
                                @CurrentUser LoginUser user) {
        log.info("【参数演示】接收到 type={}", type);

        // type=1 → 跳过校验，直接返回提示（不要求登录）
        if (type != null && type == 1) {
            Result<Object> r = new Result<>();
            r.setCode(0);
            r.setMsg("success");
            r.setData("type=1，跳过了登录校验，不需要 token");
            return r;
        }

        // 其他情况 → 正常校验，返回当前登录用户（user 由 @CurrentUser 注入）
        Result<Object> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(user);
        return r;
    }
}
