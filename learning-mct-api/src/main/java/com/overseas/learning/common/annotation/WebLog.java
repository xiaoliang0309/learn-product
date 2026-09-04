package com.overseas.learning.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @WebLog 操作日志注解 —— 对齐 qingo 的 com.qinggouyun.svem_biz_api_common.annotation.WebLog
 *
 * 【它是干嘛的？】
 *   给 Controller 方法打个标记，配套的 WebLogAspect 切面会在方法执行前后
 *   自动记录「类名.方法名 / 参数 / URL / 耗时 / 结果 / 异常 + reqId」，
 *   业务代码里一句日志都不用写。
 *
 * 【注意】注解本身是空壳（和 @CurrentUser 一个道理），
 *   真正干活的是 WebLogAspect 切面 —— 它检测到这些方法就织入日志逻辑。
 */
@Target(ElementType.METHOD)        // 只能加在「方法」上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留（切面要反射读它）
@Documented
public @interface WebLog {
}
