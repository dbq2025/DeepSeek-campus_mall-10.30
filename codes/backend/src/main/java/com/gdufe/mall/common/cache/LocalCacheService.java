package com.gdufe.mall.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存实现（默认）。带过期时间，够课程设计用。
 */
@Component
@ConditionalOnProperty(prefix = "mall.cache", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalCacheService implements CacheService {

    private record Node(String value, long expireAt) {
    }

    private final Map<String, Node> store = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value, long ttlSeconds) {
        long expireAt = ttlSeconds <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + ttlSeconds * 1000L;
        store.put(key, new Node(value, expireAt));
    }

    @Override
    public String get(String key) {
        Node node = store.get(key);
        if (node == null) {
            return null;
        }
        if (node.expireAt() < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return node.value();
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public String type() {
        return "local";
    }
}
