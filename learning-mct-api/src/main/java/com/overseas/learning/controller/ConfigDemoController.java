package com.overseas.learning.controller;

import com.overseas.learning.common.Result;
import com.overseas.learning.config.LearningBizConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置中心演示接口 —— 演示「从 Nacos 读配置 + 动态刷新」
 *
 * 【怎么用】
 *   1. 先调 GET /config/show，看当前配置值（此时是默认值）
 *   2. 到 Nacos 控制台（http://localhost:8848/nacos，nacos/nacos）
 *      → 配置管理 → 配置列表 → 新建配置：
 *         Data ID: overseas-learning.yaml
 *         Group:   DEFAULT_GROUP
 *         配置内容(YAML):
 *           learning:
 *             features:
 *               cooling-reminder: false
 *             order:
 *               max-amount: 88888
 *             tips:
 *               welcome: 这是来自 Nacos 配置中心的文案！
 *      → 发布
 *   3. 再调 GET /config/show（★ 不用重启项目），看到值变了 —— 这就是动态刷新
 *
 * 【核心】
 *   LearningBizConfig 上的 @RefreshScope 让配置改了自动生效，
 *   不用重启、不用重新打包 —— 这就是配置中心最大的价值。
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigDemoController {

    private final LearningBizConfig learningBizConfig;

    /**
     * 查看当前生效的配置
     * GET /config/show
     *
     * 改 Nacos 配置后再调，会看到值实时变化（动态刷新）
     */
    @GetMapping("/show")
    public Result<Map<String, Object>> show() {
        Map<String, Object> data = new HashMap<>();
        data.put("coolingReminder(功能开关)", learningBizConfig.getCoolingReminder());
        data.put("orderMaxAmount(下单金额上限/分)", learningBizConfig.getOrderMaxAmount());
        data.put("welcomeTip(欢迎文案)", learningBizConfig.getWelcomeTip());
        data.put("说明", "到 Nacos 控制台改 overseas-learning.yaml 后再调本接口，不用重启即可看到新值");
        return Result.success(data);
    }
}
