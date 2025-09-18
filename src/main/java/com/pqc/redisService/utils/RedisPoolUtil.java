package com.pqc.redisService.utils;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;

import com.pqc.redisService.annotation.RedisDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;

@Component
public class RedisPoolUtil {
    private final Logger logger = LoggerFactory.getLogger(RedisPoolUtil.class);
    private final TimedCache<Integer, JedisPool> jedisPoolCache = CacheUtil.newTimedCache(30 * 60 * 1000);
    private final JedisPoolConfig DEFAULT_POOL_CONFIG;
    private final String host;
    private final int port;
    private final String password;
    private final int timeout;
    private final long maxWaitMillis = 3000;

    public RedisPoolUtil(
            @Value("${redis.host:localhost}") String host,
            @Value("${redis.port:6379}") int port,
            @Value("${redis.password:}") String password,
            @Value("${redis.timeout:2000}") int timeout
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.timeout = timeout;
    }

    {
        DEFAULT_POOL_CONFIG = new JedisPoolConfig();
        DEFAULT_POOL_CONFIG.setMaxTotal(100);
        DEFAULT_POOL_CONFIG.setMaxIdle(20);
        DEFAULT_POOL_CONFIG.setMinIdle(5);

        try {
            DEFAULT_POOL_CONFIG.setMaxWait(Duration.ofMillis(maxWaitMillis));
        } catch (NoSuchMethodError e) {
            // 兼容旧版本API
            try {
                Method setMaxWaitMillisMethod = JedisPoolConfig.class.getMethod("setMaxWaitMillis", long.class);
                setMaxWaitMillisMethod.invoke(DEFAULT_POOL_CONFIG, maxWaitMillis);
            } catch (Exception ex) {
                logger.error("设置Jedis连接池最大等待时间失败", ex);
            }
        }

        DEFAULT_POOL_CONFIG.setTestOnBorrow(true);

        // 移除：原有的Props读取redis.properties逻辑

        jedisPoolCache.schedulePrune(60 * 60 * 1000);
    }

    // 移除：getPropWithDefault方法（不再需要）


    public Jedis getJedis(int dbIndex) {
        JedisPool jedisPool = getJedisPool(dbIndex);
        return jedisPool.getResource();
    }

    /**
     * 根据数据库索引获取连接池
     */
    private JedisPool getJedisPool(int dbIndex) {
        JedisPool jedisPool = jedisPoolCache.get(dbIndex);
        if (jedisPool == null) {
            synchronized (RedisPoolUtil.class) {
                jedisPool = jedisPoolCache.get(dbIndex);
                if (jedisPool == null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(DEFAULT_POOL_CONFIG.getMaxTotal());
                    poolConfig.setMaxIdle(DEFAULT_POOL_CONFIG.getMaxIdle());
                    poolConfig.setMinIdle(DEFAULT_POOL_CONFIG.getMinIdle());

                    // 兼容处理最大等待时间的设置
                    try {
                        poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
                    } catch (NoSuchMethodError e) {
                        try {
                            Method setMaxWaitMillisMethod = JedisPoolConfig.class.getMethod("setMaxWaitMillis", long.class);
                            setMaxWaitMillisMethod.invoke(poolConfig, maxWaitMillis);
                        } catch (Exception ex) {
                            logger.error("设置Jedis连接池最大等待时间失败", ex);
                        }
                    }

                    poolConfig.setTestOnBorrow(DEFAULT_POOL_CONFIG.getTestOnBorrow());

                    // 创建连接池（使用注入的host/port等配置）
                    if (StrUtil.isNotBlank(password)) {
                        jedisPool = new JedisPool(poolConfig, host, port, timeout, password, dbIndex);
                    } else {
                        jedisPool = new JedisPool(poolConfig, host, port, timeout, null, dbIndex);
                    }

                    jedisPoolCache.put(dbIndex, jedisPool);
                }
            }
        }
        return jedisPool;
    }

    /**
     * 根据注解和方法参数获取目标数据库索引
     */
    public int getTargetDbIndex(Class<?> clazz, Method method, Object[] args) {
        // 方法上的注解优先
        RedisDB methodAnnotation = method.getAnnotation(RedisDB.class);
        RedisDB classAnnotation = clazz.getAnnotation(RedisDB.class);

        int dbIndex = 0;
        String paramName = "";

        // 解析注解信息
        if (methodAnnotation != null) {
            dbIndex = methodAnnotation.value();
            paramName = methodAnnotation.paramName();
        } else if (classAnnotation != null) {
            dbIndex = classAnnotation.value();
            paramName = classAnnotation.paramName();
        }

        // 如果指定了参数名，则从参数中获取数据库索引
        if (StrUtil.isNotBlank(paramName) && args != null && args.length > 0) {
            try {
                // 使用Java反射的Parameter类获取参数名
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    if (paramName.equals(parameters[i].getName()) && args[i] instanceof Number) {
                        dbIndex = ((Number) args[i]).intValue();
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("获取方法参数名时发生异常，使用默认数据库索引", e);
            }
        }

        // 添加数据库索引范围校验（Redis默认0-15）
        if (dbIndex < 0 || dbIndex > 15) {
            logger.warn("Redis数据库索引{}超出有效范围(0-15)，自动修正为0", dbIndex);
            return 0;
        }
        return dbIndex;
    }

    /**
     * 关闭Jedis连接
     */
    public  void closeJedis(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (Exception e) {
                logger.error("关闭Jedis连接时发生异常", e);
            }
        }
    }

}
