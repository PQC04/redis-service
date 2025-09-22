package com.pqc.redisService.utils;

import com.pqc.redisService.annotation.RedisDB;
import com.pqc.redisService.aspect.RedisDatabaseAspect;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import java.util.Map;


/**
 * Redis工具类
 * <p>
 * 提供Redis数据库的基本操作方法，包括获取连接、保存数据、获取数据、删除数据和清空数据库等功能。
 * 使用自定义的{@link RedisDB}注解来指定操作的数据库编号。
 * </p>
 *
 * @author PQC
 * @version 1.0
 * @since 1.0
 */
@Component
public class RedisUtils {

    /**
     * 获取Redis连接实例
     * <p>
     * 从Redis上下文获取当前线程绑定的Jedis连接实例，用于执行Redis操作。
     * </p>
     *
     * @return Jedis实例，用于与Redis服务器交互
     */
    public Jedis getJedis() {
        return RedisDatabaseAspect.RedisContext.getJedis();
    }

    // ------------------------ String类型操作 ------------------------

    /**
     * 保存键值对到指定Redis数据库
     * <p>
     * 将指定的键值对保存到由dbNum参数指定的Redis数据库中。
     * </p>
     *
     * @param key 键名
     * @param value 键值
     * @param dbNum Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void save(String key, String value, Integer dbNum) {
        getJedis().set(key, value);
    }

    /**
     * 从指定Redis数据库删除键
     * <p>
     * 从由dbNum参数指定的Redis数据库中删除指定的键。
     * </p>
     *
     * @param key 要删除的键名
     * @param dbNum Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void delete(String key, Integer dbNum) {
        getJedis().del(key);
    }

    /**
     * 从指定Redis数据库获取键值
     * <p>
     * 从由dbNum参数指定的Redis数据库中获取指定键的值。
     * </p>
     *
     * @param key 要获取的键名
     * @param dbNum Redis数据库编号
     * @return 键对应的值，如果键不存在则返回null
     */
    @RedisDB(paramName = "dbNum")
    public String get(String key, Integer dbNum) {
        return getJedis().get(key);
    }

    // ------------------------ Hash类型操作 ------------------------

    /**
     * 存储Hash类型数据
     * <p>
     * 在指定的Hash键中存储一个字段和值。
     * </p>
     *
     * @param hashKey Hash键名
     * @param field 字段名
     * @param value 字段值
     * @param dbNum Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void hset(String hashKey, String field, String value, Integer dbNum) {
        getJedis().hset(hashKey, field, value);
    }

    /**
     * 批量存储Hash类型数据
     * <p>
     * 在指定的Hash键中批量存储多个字段和值。
     * </p>
     *
     * @param hashKey Hash键名
     * @param hash 包含多个字段和值的Map
     * @param dbNum Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void hmset(String hashKey, Map<String, String> hash, Integer dbNum) {
        getJedis().hmset(hashKey, hash);
    }

    /**
     * 获取Hash类型数据中的指定字段值
     * <p>
     * 获取指定Hash键中指定字段的值。
     * </p>
     *
     * @param hashKey Hash键名
     * @param field 字段名
     * @param dbNum Redis数据库编号
     * @return 字段对应的值，如果字段不存在则返回null
     */
    @RedisDB(paramName = "dbNum")
    public String hget(String hashKey, String field, Integer dbNum) {
        return getJedis().hget(hashKey, field);
    }

    /**
     * 删除Hash类型数据中的指定字段
     * <p>
     * 删除指定Hash键中的一个或多个字段。
     * </p>
     *
     * @param hashKey Hash键名
     * @param fields 要删除的字段名数组
     * @param dbNum Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void hdel(String hashKey, Integer dbNum, String... fields) {
        getJedis().hdel(hashKey, fields);
    }

    // ------------------------ List类型操作 ------------------------

    /**
     * 在List左侧添加元素
     * <p>
     * 在指定List的左侧（头部）添加一个或多个元素。
     * </p>
     *
     * @param listKey List键名
     * @param values 要添加的元素值
     * @param dbNum Redis数据库编号
     * @return 添加后的List长度
     */
    @RedisDB(paramName = "dbNum")
    public Long lpush(String listKey, Integer dbNum, String... values) {
        return getJedis().lpush(listKey, values);
    }

    /**
     * 在List右侧添加元素
     * <p>
     * 在指定List的右侧（尾部）添加一个或多个元素。
     * </p>
     *
     * @param listKey List键名
     * @param values 要添加的元素值
     * @param dbNum Redis数据库编号
     * @return 添加后的List长度
     */
    @RedisDB(paramName = "dbNum")
    public Long rpush(String listKey, Integer dbNum, String... values) {
        return getJedis().rpush(listKey, values);
    }

    /**
     * 从List左侧弹出元素
     * <p>
     * 从指定List的左侧（头部）弹出一个元素。
     * </p>
     *
     * @param listKey List键名
     * @param dbNum Redis数据库编号
     * @return 弹出的元素值，如果List为空则返回null
     */
    @RedisDB(paramName = "dbNum")
    public String lpop(String listKey, Integer dbNum) {
        return getJedis().lpop(listKey);
    }

    /**
     * 从List右侧弹出元素
     * <p>
     * 从指定List的右侧（尾部）弹出一个元素。
     * </p>
     *
     * @param listKey List键名
     * @param dbNum Redis数据库编号
     * @return 弹出的元素值，如果List为空则返回null
     */
    @RedisDB(paramName = "dbNum")
    public String rpop(String listKey, Integer dbNum) {
        return getJedis().rpop(listKey);
    }

    // ------------------------ Set类型操作 ------------------------

    /**
     * 向Set中添加元素
     * <p>
     * 向指定的Set中添加一个或多个元素。
     * </p>
     *
     * @param setKey Set键名
     * @param members 要添加的元素
     * @param dbNum Redis数据库编号
     * @return 添加的新元素数量
     */
    @RedisDB(paramName = "dbNum")
    public Long sadd(String setKey,  Integer dbNum,String... members) {
        return getJedis().sadd(setKey, members);
    }

    /**
     * 从Set中删除元素
     * <p>
     * 从指定的Set中删除一个或多个元素。
     * </p>
     *
     * @param setKey Set键名
     * @param members 要删除的元素
     * @param dbNum Redis数据库编号
     * @return 删除的元素数量
     */
    @RedisDB(paramName = "dbNum")
    public Long srem(String setKey, Integer dbNum, String... members) {
        return getJedis().srem(setKey, members);
    }

    /**
     * 判断元素是否在Set中
     * <p>
     * 判断指定元素是否存在于指定的Set中。
     * </p>
     *
     * @param setKey Set键名
     * @param member 要判断的元素
     * @param dbNum Redis数据库编号
     * @return 如果元素存在则返回true，否则返回false
     */
    @RedisDB(paramName = "dbNum")
    public Boolean sismember(String setKey, String member, Integer dbNum) {
        return getJedis().sismember(setKey, member);
    }

    // ------------------------ Sorted Set类型操作 ------------------------

    /**
     * 向Sorted Set中添加元素
     * <p>
     * 向指定的Sorted Set中添加一个带有分数的元素。
     * </p>
     *
     * @param zsetKey Sorted Set键名
     * @param score 元素的分数
     * @param member 元素值
     * @param dbNum Redis数据库编号
     * @return 添加是否成功
     */
    @RedisDB(paramName = "dbNum")
    public Boolean zadd(String zsetKey, double score, String member, Integer dbNum) {
        return getJedis().zadd(zsetKey, score, member) > 0;
    }

    /**
     * 从Sorted Set中删除元素
     * <p>
     * 从指定的Sorted Set中删除一个或多个元素。
     * </p>
     *
     * @param zsetKey Sorted Set键名
     * @param members 要删除的元素
     * @param dbNum Redis数据库编号
     * @return 删除的元素数量
     */
    @RedisDB(paramName = "dbNum")
    public Long zrem(String zsetKey, Integer dbNum, String... members) {
        return getJedis().zrem(zsetKey, members);
    }

    /**
     * 更新Sorted Set中元素的分数
     * <p>
     * 更新指定Sorted Set中元素的分数。
     * </p>
     *
     * @param zsetKey Sorted Set键名
     * @param score 新的分数
     * @param member 要更新分数的元素
     * @param dbNum Redis数据库编号
     * @return 更新是否成功
     */
    @RedisDB(paramName = "dbNum")
    public Boolean zaddScore(String zsetKey, double score, String member, Integer dbNum) {
        // 先删除再添加，实现更新分数的功能
        getJedis().zrem(zsetKey, member);
        return getJedis().zadd(zsetKey, score, member) > 0;
    }

    // ------------------------ 通用操作 ------------------------

    /**
     * 清空指定的Redis数据库
     * <p>
     * 清空由dbNum参数指定的Redis数据库中的所有键值对。
     * <strong>注意：此操作不可逆，请谨慎使用。</strong>
     * </p>
     *
     * @param dbNum 要清空的Redis数据库编号
     */
    @RedisDB(paramName = "dbNum")
    public void clearAll(Integer dbNum) {
        getJedis().flushDB();
    }

}
