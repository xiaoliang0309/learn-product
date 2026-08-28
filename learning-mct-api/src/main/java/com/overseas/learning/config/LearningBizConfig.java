package com.overseas.learning.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 业务配置读取类 —— 演示「从 Nacos 配置中心读配置 + 动态刷新」
 *
 * 【★ 两个关键注解】
 *   @RefreshScope：标在类上，表示「这个 Bean 支持动态刷新」。
 *                  你在 Nacos 控制台改了配置并发布，
 *                  这个 Bean 里的值会「不用重启项目」自动变成新值。
 *
 *   @Value("${key:默认值}")：读取某个配置项。
 *                  冒号后面是「默认值」——Nacos 里没配这个 key 时用它兜底。
 *
 * 【读取优先级】
 *   Nacos 配置中心  >  本地 application.yml  >  @Value 里的默认值
 *
 * 【对齐 qingo】
 *   qingo 把「会变的业务参数」（比如账单是否排除当月、限流阈值、功能开关）
 *   放到 Nacos 配置中心，改完不用发版重启就生效。
 *   例子：qingo 的 qingo.bill-month.exclude-current-month
 */
@Data
@Component
@RefreshScope   // ★ 关键：Nacos 配置改了，这个 Bean 自动刷新，不用重启
public class LearningBizConfig {

    /**
     * 演示配置1：功能开关（是否开启强制制冷提醒）
     * 在 Nacos 的 overseas-learning.yaml 里配 learning.features.cooling-reminder: true/false
     * 本地没配就用默认值 true
     */
    @Value("${learning.features.cooling-reminder:true}")
    private Boolean coolingReminder;

    /**
     * 演示配置2：业务参数（下单金额上限，单位分）
     * 在 Nacos 里配 learning.order.max-amount: 100000
     * 本地没配就用默认值 100000
     */
    @Value("${learning.order.max-amount:100000}")
    private Long orderMaxAmount;

    /**
     * 演示配置3：提示文案（多环境可能不同）
     * 在 Nacos 里配 learning.tips.welcome: xxx
     */
    @Value("${learning.tips.welcome:欢迎使用学习项目（本地默认值）}")
    private String welcomeTip;
}
