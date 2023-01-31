package com.asyncexport.boot.utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * redis 工具类.
 * @author Pete.Lee 2017/9/5 15:01
 */
@Slf4j
public class RedisUtil {

    private static StringRedisTemplate template = ContextUtil.getBean("StringRedisTemplate", StringRedisTemplate.class);

    public static String get(final String key) {
        String obj = null;
        try {
            obj = template.execute((final RedisConnection c) -> {
                final StringRedisSerializer serializer = new StringRedisSerializer();
                final byte[] data = c.get(serializer.serialize(key));
                c.close();
                return serializer.deserialize(data);
            });
        } catch (Exception ex) {
            log.error("get redis error, key : {}", key);
        }
        return obj;
    }

    public static Boolean setNx(final String key, final String value) {
        Boolean b = false;
        try {
            b = template.execute((final RedisConnection c) -> {
                final StringRedisSerializer serializer = new StringRedisSerializer();
                final Boolean success = c.setNX(serializer.serialize(key), serializer.serialize(value));
                c.close();
                return success;
            });
        } catch (Exception e) {
            log.error("setNX redis error, key : {}", key);
        }
        return b;
    }

    public static String getSet(final String key, final String value) {
        String obj = null;
        try {
            obj = template.execute((final RedisConnection c) -> {
                final StringRedisSerializer serializer = new StringRedisSerializer();
                final byte[] ret = c.getSet(serializer.serialize(key), serializer.serialize(value));
                c.close();
                return serializer.deserialize(ret);
            });
        } catch (Exception ex) {
            log.error("setNX redis error, key : {}", key);
        }
        return obj;
    }

    public static Boolean del(final String key) {
        Boolean obj = null;
        try {
            obj = template.execute((final RedisConnection c) -> {
                final StringRedisSerializer serializer = new StringRedisSerializer();
                return c.del(serializer.serialize(key)) > 0;
            });
        } catch (Exception ex) {
            log.error("del redis error, key : {}", key);
        }
        return obj;
    }

    public static Long leftPush(final String key, final String value) {
        return template.opsForList().leftPush(key, value);
    }

    public static Long rightPush(final String key, final String value) {
        return template.opsForList().rightPush(key, value);
    }

    public static String leftPop(final String key) {
        return template.opsForList().leftPop(key);
    }

    public static String rightPop(final String key) {
        return template.opsForList().rightPop(key);
    }
  /*public RedisUtil() {
    final JedisConnectionFactory factory = new JedisConnectionFactory();
    factory.setHostName("xxxx");
    factory.setPort(6379);
    factory.setDatabase(2);
    factory.afterPropertiesSet();
    template = new StringRedisTemplate(factory);
    template.afterPropertiesSet();
    // System.out.println(template.boundValueOps("自增Key").increment(1));
  }*/
}