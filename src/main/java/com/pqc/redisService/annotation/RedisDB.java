package com.pqc.redisService.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisDB {

    // 通过注解值指定数据库索引
    int value() default -1;

    // 通过方法参数名指定数据库索引（参数优先级高于value）
    String paramName() default "";
}