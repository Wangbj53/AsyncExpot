package com.asyncexport.boot.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Redis辅助类
 */
@Service
public class RedisHelper {

    @Value("${spring.application.name}")
    private String applicationName;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    protected String comboKey(String originKey) {
        return this.comboKey(this.applicationName, originKey);
    }

    protected String comboKey(String prefix, String originKey) {
        return prefix + ":" + originKey;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(this.redisTemplate.hasKey(this.comboKey(key)));
    }


    public void set(String key, Object data, long timeout, TimeUnit unit) throws RuntimeException {
        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            operations.set(key, data, timeout, unit);
        } catch (Exception var8) {
            var8.printStackTrace();
            throw new RuntimeException("Redis新增缓存数据异常：【key：" + key + "】");
        }
    }


    public void delete(String key) throws RuntimeException {
        try {
            key = this.comboKey(key);
            this.redisTemplate.delete(key);
        } catch (Exception var4) {
            var4.printStackTrace();
            throw new RuntimeException("Redis删除缓存异常：【key：" + key + "】");
        }
    }

    public Long incr(String key) throws RuntimeException {
        Long incr = 0L;

        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            incr = operations.increment(key);
            return incr;
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException("Redis incr缓存数据异常：【key：" + key + "】");
        }
    }

    public <T> List<T> getList(String key, Class<T> classz) throws RuntimeException {
        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            Object obj = operations.get(key);
            if (obj != null) {
                return JSON.parseArray(JSON.toJSONString(obj), classz);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            throw new RuntimeException("Redis获取(getList)缓存数据（对象集合）异常：【key：" + key + "，classz：" + classz + "】");
        }

        return Collections.emptyList();
    }


    public void hset(String key, String hashKey, Object obj) throws RuntimeException {
        try {
            key = this.comboKey(key);
            HashOperations<String, String, Object> operations = this.redisTemplate.opsForHash();
            operations.put(key, hashKey, obj);
        } catch (Exception var6) {
            var6.printStackTrace();
            throw new RuntimeException("Redis新增Hash缓存数据异常：【key：" + key + "，hashKey：" + hashKey + "】");
        }
    }

    public void hdel(String key, String hashKey) throws RuntimeException {
        try {
            key = this.comboKey(key);
            HashOperations<String, String, Object> operations = this.redisTemplate.opsForHash();
            operations.delete(key, new Object[]{hashKey});
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException("Redis删除Hash缓存数据异常：【key：" + key + "，hashKey：" + hashKey + "】");
        }
    }

    public <T> Map<String, T> hgetall(String key, Class<T> classz) {
        try {
            key = this.comboKey(key);
            HashOperations<String, String, T> operations = this.redisTemplate.opsForHash();
            return operations.entries(key);
        } catch (Exception var5) {
            var5.printStackTrace();
            // throw new RuntimeException("Redis获取Hash缓存数据（Map对象）异常：【key：" + key + "，classz：" + classz + "】");
            return Collections.emptyMap();
        }
    }

    public Object get(String key) throws RuntimeException {
        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            return operations.get(key);
        } catch (Exception var4) {
            var4.printStackTrace();
            throw new RuntimeException("Redis获取缓存数据（Object）异常：【key：" + key + "】");
        }
    }

    public void set(String key, Object data) throws RuntimeException {
        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            operations.set(key, data);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException("Redis新增缓存数据异常：【key：" + key + "】");
        }
    }

    public <T> T get(String key, Class<T> classz) throws RuntimeException {
        try {
            key = this.comboKey(key);
            ValueOperations<String, Object> operations = this.redisTemplate.opsForValue();
            Object obj = operations.get(key);
            return JSON.parseObject(JSON.toJSONString(obj), classz);
        } catch (Exception var6) {
            var6.printStackTrace();
            throw new RuntimeException("Redis获取(get)缓存数据（对象）异常：【key：" + key + "，classz：" + classz + "】");
        }
    }


}

