# Redis 二次封装工具

基于 Hutool 和 Jedis 的 Redis 操作工具类，提供数据库索引动态切换、连接池管理等功能，简化 Redis 操作。

## 功能特点
- 支持通过注解 `@RedisDB` 动态指定 Redis 数据库索引
- 基于连接池管理 Redis 连接，优化性能
- 集成 Hutool 工具类，简化数据转换与缓存操作
- 自动校验数据库索引范围（0-15），避免越界

## 依赖要求
- JDK 21+
- Spring Boot 3.5.5+
- Hutool 5.8.25+
- Jedis（版本由 Spring Boot 自动管理）

## 安装方式
在 Maven 项目的中添加依赖：
```xml
<dependency>
    <groupId>com.pqc</groupId>
    <artifactId>redis-service</artifactId>
    <version>1.0.0</version>
</dependency>