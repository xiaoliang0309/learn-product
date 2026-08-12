package com.overseas.learning.service;

/**
 * Redis 操作封装 —— 与 qingo 的 RedisService 方法签名保持一致
 *
 * 【对应关系】
 *   本接口的方法名、参数、返回值都照着 qingo-svem-biz-redis-common 的
 *   RedisService 写的，你学会这套，看 qingo 代码就是同一个套路。
 *
 * 【为什么要把 RedisTemplate 再包一层？】
 *   和「数据层 Service 包 Mapper」是同一个思想：
 *     - 业务层（Biz）不直接操作 RedisTemplate 这种底层 API
 *     - 统一收口，将来换实现（如 Redisson、加埋点）只改这一处
 *     - 方法名用业务语义（putIfAbsent 防重、incr 自增），可读性高
 */
public interface RedisService {

    /**
     * 存值（带过期时间）
     *
     * @param key    键
     * @param value  值（对象会被序列化成 JSON）
     * @param second 过期时间（秒）
     */
    boolean set(String key, Object value, long second);

    /**
     * 取值
     *
     * @return 值对象，不存在返回 null
     */
    Object get(String key);

    /**
     * 取值并转成指定类型（★ 常用）
     *
     * 【为什么要这个重载？】
     *   value 用 JSON 序列化存的是「无类型」的文本，直接 get() 读出来
     *   Jackson 会转成 LinkedHashMap，强转成 Merchant 会报 ClassCastException。
     *   所以提供带类型的 get：内部用 ObjectMapper.convertValue 转成你要的类型。
     *
     * @param key  键
     * @param type 目标类型，如 Merchant.class
     * @return 指定类型的对象，不存在返回 null
     */
    <T> T get(String key, Class<T> type);

    /**
     * 删除
     */
    void remove(String key);

    /**
     * 判断 key 是否存在
     */
    boolean hasKey(String key);

    /**
     * ★ 防重复提交（重点）
     *
     * 语义：key 不存在时才写入成功；已存在则写入失败。
     * 对应 Redis 原生命令： SET key value EX 秒 NX
     *
     * qingo 里方法名就叫 putIfAbsent，用来防「接口被重复调用」：
     *   第一次调用 → 写入成功返回 true → 放行
     *   第二次调用（key 还在）→ 写入失败返回 false → 拦截「请勿重复提交」
     *
     * @param key    键（一般带业务唯一标识，如 submit:onboarding:商户ID）
     * @param value  值（随便存个标记即可）
     * @param second 过期时间（秒），防止锁永远不释放
     * @return true=写入成功（是第一次），false=已存在（重复提交）
     */
    Boolean putIfAbsent(String key, String value, long second);

    /**
     * 自增 1 并返回最新值（用于生成编号/序号）
     *
     * 对应 Redis 命令 INCR，qingo 用它做分布式 ID 发号器（getLoopId）。
     * key 不存在时会从 1 开始。
     *
     * @return 自增后的值
     */
    Long incr(String key);
}
