package com.pqc.redisService.annotation;

import java.lang.annotation.*;

/**
 * Redis数据库索引指定注解
 * <p>
 * 用于标记类或方法需要操作的Redis数据库索引，支持两种指定方式：
 * 1. 直接通过value属性指定固定的数据库索引
 * 2. 通过paramName属性指定方法参数名，从参数值动态获取数据库索引
 * 其中，参数方式的优先级高于直接指定value的方式
 * </p>
 * 可用于类级别（所有方法生效）或方法级别（仅当前方法生效，覆盖类级别配置）
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisDB {

    /**
     * 固定的Redis数据库索引
     * <p>
     * 默认值为-1，表示未指定固定索引，此时将尝试通过paramName获取
     * </p>
     * @return 数据库索引值，范围通常为0-15（Redis默认配置）
     */
    int value() default -1;

    /**
     * 用于获取数据库索引的方法参数名
     * <p>
     * 若指定该属性，将优先从方法参数中获取对应名称的参数值作为数据库索引
     * 适用于需要动态切换数据库的场景
     * </p>
     * @return 参数名称，默认为空字符串表示不使用参数方式
     */
    String paramName() default "";
}