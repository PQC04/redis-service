package com.pqc.redisService.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Redis服务自动配置类
 * <p>
 * 该类是Redis服务模块的核心配置类，负责启用Spring的自动组件扫描机制，
 * 以便Spring容器能够发现并注册指定包路径下的所有组件（如Service、Repository、Component等）。
 * </p>
 * 
 * @author PQC
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ComponentScan(basePackages = "com.pqc.redisService")
public class RedisAutoConfiguration {
}
