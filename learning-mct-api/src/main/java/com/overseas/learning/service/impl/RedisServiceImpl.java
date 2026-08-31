package com.overseas.learning.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RedisService 实现 —— 对齐 qingo 的 RedisServiceImpl
 *
 * 底层用 Spring 的 RedisTemplate 操作 Redis。
 * putIfAbsent 的实现直接照搬了 qingo 的写法（用 connection.execute 发原生命令），
 * 因为这是「SET ... NX EX」这种组合命令，RedisTemplate 没有现成方法。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    // RedisConfig 里定义的那个 RedisTemplate<String, Object>
    private final RedisTemplate<String, Object> redisTemplate;

    // 用于 get(key, Class) 时把 LinkedHashMap 转成目标实体
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean set(String key, Object value, long second) {
        try {
            redisTemplate.opsForValue().set(key, value, second, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("Redis set 异常: key={}", key, e);
            return false;
        }
    }

    @Override
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 带类型的取值。
     *
     * 坑点（实测踩过）：JSON 反序列化「无类型」的值时，Jackson 默认产出
     * LinkedHashMap，直接 (Merchant) cached 会抛 ClassCastException。
     * 用 ObjectMapper.convertValue 能把 Map 安全地转成目标实体。
     */
    @Override
    public <T> T get(String key, Class<T> type) {
        Object raw = get(key);
        if (raw == null) {
            return null;
        }
        // 类型已经匹配就直接返回；否则（LinkedHashMap）做一次转换
        if (type.isInstance(raw)) {
            return type.cast(raw);
        }
        return objectMapper.convertValue(raw, type);
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey 异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 防重复提交 —— 实现照搬 qingo（qingo RedisServiceImpl 第 92 行就是这么写的）
     *
     * 用 Spring Data 的高层 API：opsForValue().setIfAbsent(key, value, 秒, SECONDS)
     *   它底层就是原子的「SET key value NX EX 秒」，一句话表达「不存在才写入 + 带过期」。
     *
     * 【为什么不用 connection.execute 发原生命令？】
     *   之前用 redisTemplate.execute(connection -> connection.execute("set",...))，
     *   这种「透传原生命令」的写法只有 Lettuce/Jedis 连接支持；
     *   本项目为分布式锁引入了 Redisson，Redisson 的 RedissonConnection 不支持
     *   任意命令透传，会抛 UnsupportedOperationException（你刚遇到的那个报错）。
     *   而 setIfAbsent 是标准高层 API，Lettuce / Jedis / Redisson 三种连接都兼容。
     */
    @Override
    public Boolean putIfAbsent(String key, String value, long expire) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
