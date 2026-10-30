package com.gdufe.mall.common.cache;

/**
 * 缓存抽象。默认本地实现，Redis 作为可选装配（mall.cache.type=redis）。
 * 这样本机没有 Redis 也能跑通全部功能，报告需要 Redis 时改一行配置即可。
 */
public interface CacheService {

    void set(String key, String value, long ttlSeconds);

    String get(String key);

    void delete(String key);

    /** 健康检查用 */
    boolean ping();

    /** local / redis */
    String type();
}
