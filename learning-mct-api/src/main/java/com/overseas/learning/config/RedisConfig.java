package com.overseas.learning.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置 —— 序列化方式
 *
 * 【为什么需要这个类？】
 *   Spring 默认的 RedisTemplate 用 JDK 序列化，存进去的 key/value 是一串
 *   二进制乱码（\xAC\xED\x00\x05...），你用 redis-cli 看会一脸懵。
 *   配置后：
 *     key   → 用 String 序列化（redis-cli 里能直接看懂，如 mct:1）
 *     value → 用 JSON 序列化（对象存成 {"id":1,"fullName":"xxx"}，可读）
 *
 * 【和 qingo 的对应】
 *   qingo 的 RedisAutoConfiguration 干的是同一件事：
 *   定义一个 RedisTemplate<String, Serializable>，指定 key/value 的序列化器。
 *   学习项目简化为最常见的 String key + JSON value 组合。
 *
 * 【什么是序列化？】
 *   Java 对象要存进 Redis（内存里只能存字节），必须先「转成字节/字符串」，
 *   这个过程叫序列化；取出来时再「转回对象」，叫反序列化。
 *   序列化方式决定了你在 redis-cli 里看到的内容长什么样。
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // ===== key 用 String 序列化 =====
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // ===== value 用 JSON 序列化 =====
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 让所有字段都能被序列化（包括 private）
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jsonSerializer.setObjectMapper(om);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
