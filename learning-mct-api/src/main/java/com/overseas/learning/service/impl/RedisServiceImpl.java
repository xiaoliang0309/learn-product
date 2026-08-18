package com.overseas.learning.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overseas.learning.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
     * 防重复提交 —— 实现照搬 qingo
     *
     * 为什么用 connection.execute 而不是现成 API？
     *   「SET key value EX 秒 NX」是一条原子命令（要么全做要么全不做），
     *   RedisTemplate 的高级 API 不好一次表达「NX + EX」组合，
     *   所以 qingo 直接用底层 connection 发原生命令，保证原子性。
     *   原子性很重要：判断「是否存在」和「写入」必须是一步，否则并发下会失效。
     */
    @Override
    public Boolean putIfAbsent(String key, String value, long expire) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            Object obj = connection.execute("set",
                    keySerializer.serialize(key),
                    valueSerializer.serialize(value),
                    "NX".getBytes(StandardCharsets.UTF_8),   // NX = 不存在才写入
                    "EX".getBytes(StandardCharsets.UTF_8),   // EX = 设置过期秒数
                    String.valueOf(expire).getBytes(StandardCharsets.UTF_8));
            // 写入成功返回 "OK"，已存在返回 null → 转成 boolean
            return obj != null;
        });
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
