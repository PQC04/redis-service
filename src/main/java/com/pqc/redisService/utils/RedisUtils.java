package com.pqc.redisService.utils;

import com.pqc.redisService.aspect.RedisDatabaseAspect;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class RedisUtils {
    public Jedis getJedis() {
        return RedisDatabaseAspect.RedisContext.getJedis();
    }
}
