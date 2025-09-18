package com.pqc.redisService.aspect;

import com.pqc.redisService.utils.RedisPoolUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;

/**
 * Redis数据库索引切面，处理RedisDatabase注解
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RedisDatabaseAspect {

    private final Logger logger = LoggerFactory.getLogger(RedisDatabaseAspect.class);
    private final RedisPoolUtil redisPoolUtil;



    /**
     * 定义切入点：拦截所有带有RedisDatabase注解的方法
     */
    @Pointcut("@annotation(com.pqc.redisService.annotation.RedisDB) || @within(com.pqc.redisService.annotation.RedisDB)")
    public void redisDatabasePointcut() {}

    /**
     * 环绕通知：处理Redis连接的获取和释放
     */
    @Around("redisDatabasePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取目标类和方法信息
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 获取目标数据库索引
        int dbIndex = redisPoolUtil.getTargetDbIndex(targetClass, targetMethod, args);
        logger.debug("使用Redis数据库索引: {}", dbIndex);

        // 获取Jedis连接
        Jedis jedis = null;
        try {
            jedis = redisPoolUtil.getJedis(dbIndex);
            // 将Jedis实例设置到ThreadLocal或通过参数传递给目标方法
            // 这里假设目标方法通过ThreadLocal获取Jedis连接
            RedisContext.setJedis(jedis);

            // 执行目标方法
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.error("执行Redis操作异常", e);
            throw e;
        } finally {
            // 清理ThreadLocal并关闭连接
            RedisContext.removeJedis();
            if (jedis != null) {
                redisPoolUtil.closeJedis(jedis);
            }
        }
    }

    /**
     * Redis上下文工具类，用于ThreadLocal存储当前线程的Jedis连接
     */
    public static class RedisContext {
        private static final ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<>();

        public static void setJedis(Jedis jedis) {
            jedisThreadLocal.set(jedis);
        }

        public static Jedis getJedis() {
            return jedisThreadLocal.get();
        }

        public static void removeJedis() {
            jedisThreadLocal.remove();
        }
    }
}