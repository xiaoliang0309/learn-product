package com.overseas.learning.common;

/**
 * Redis Key 统一管理
 *
 * 【为什么 key 要集中定义？】
 *   qingo 项目里所有 Redis key 都放在 RedisIDKeyEnum / RedisCacheKeyConstant 里，
 *   不会散落在业务代码各处。好处：
 *     - 一眼看全系统用了哪些 key，避免重复/冲突
 *     - 改前缀、改过期时间只改这一处
 *     - key 有统一前缀，redis-cli 里好查（KEYS mct:*）
 *
 * 【key 命名规范】项目:模块:业务:唯一标识
 *   例如：learning:merchant:1  → 学习项目-商户模块-id为1的商户缓存
 */
public class RedisKeys {

    /** key 统一前缀 */
    private static final String PREFIX = "learning";

    /** 商户缓存：learning:merchant:{id} */
    public static String merchant(Long id) {
        return PREFIX + ":merchant:" + id;
    }

    /** 进件防重复提交：learning:submit:onboarding:{merchantId} */
    public static String submitOnboarding(Long merchantId) {
        return PREFIX + ":submit:onboarding:" + merchantId;
    }

    /** 商户编号自增：learning:id:merchant */
    public static String merchantIncr() {
        return PREFIX + ":id:merchant";
    }

    private RedisKeys() {
    }
}
