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
 * Redis数据库索引切面类
 * <p>
 * 用于处理@RedisDB注解，实现基于注解的Redis数据库索引动态切换
 * 通过AOP环绕通知，在目标方法执行前后管理Redis连接的获取、切换和释放
 * </p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RedisDatabaseAspect {

    /** 日志记录器 */
    private final Logger logger = LoggerFactory.getLogger(RedisDatabaseAspect.class);

    /** Redis连接池工具类，用于获取和管理Jedis连接 */
    private final RedisPoolUtil redisPoolUtil;


    /**
     * 定义切入点
     * <p>
     * 匹配所有被@RedisDB注解标记的方法，或包含@RedisDB注解的类中的所有方法
     * </p>
     */
    @Pointcut("@annotation(com.pqc.redisService.annotation.RedisDB) || @within(com.pqc.redisService.annotation.RedisDB)")
    public void redisDatabasePointcut() {}

    /**
     * 环绕通知方法
     * <p>
     * 实现Redis连接的获取、数据库切换、ThreadLocal绑定、目标方法执行及资源清理的完整流程
     * </p>
     * @param joinPoint 连接点对象，包含目标方法的信息
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法执行过程中抛出的异常
     */
    @Around("redisDatabasePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取目标类和方法的元数据信息
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析目标方法需要使用的Redis数据库索引
        int dbIndex = redisPoolUtil.getTargetDbIndex(targetClass, targetMethod, args);
        logger.debug("当前操作使用的Redis数据库索引: {}", dbIndex);

        // 声明Jedis连接对象
        Jedis jedis = null;
        try {
            // 从连接池获取指定数据库的Jedis连接
            jedis = redisPoolUtil.getJedis(dbIndex);

            // 将Jedis连接绑定到当前线程的ThreadLocal中，供后续操作使用
            RedisContext.setJedis(jedis);

            // 执行目标方法并返回结果
            return joinPoint.proceed();
        } catch (Exception e) {
            // 记录操作异常并向上抛出
            logger.error("执行Redis相关操作时发生异常", e);
            throw e;
        } finally {
            // 清除当前线程的Jedis连接绑定
            RedisContext.removeJedis();
            // 归还Jedis连接到连接池
            if (jedis != null) {
                redisPoolUtil.closeJedis(jedis);
            }
        }
    }

    /**
     * Redis上下文工具类
     * <p>
     * 基于ThreadLocal实现当前线程的Jedis连接存储，
     * 提供连接的设置、获取和清除操作，确保线程安全
     * </p>
     */
    public static class RedisContext {
        /** ThreadLocal存储容器，键为线程，值为当前线程使用的Jedis连接 */
        private static final ThreadLocal<Jedis> jedisThreadLocal = new ThreadLocal<>();

        /**
         * 将Jedis连接绑定到当前线程
         * @param jedis 待绑定的Jedis连接对象
         */
        public static void setJedis(Jedis jedis) {
            jedisThreadLocal.set(jedis);
        }

        /**
         * 获取当前线程绑定的Jedis连接
         * @return 当前线程的Jedis连接，若未绑定则返回null
         */
        public static Jedis getJedis() {
            return jedisThreadLocal.get();
        }

        /**
         * 清除当前线程绑定的Jedis连接
         * <p>
         * 用于防止ThreadLocal内存泄漏，必须在操作完成后调用
         * </p>
         */
        public static void removeJedis() {
            jedisThreadLocal.remove();
        }
    }
}