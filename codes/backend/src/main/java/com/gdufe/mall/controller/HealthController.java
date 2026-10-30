package com.gdufe.mall.controller;

import com.gdufe.mall.common.cache.CacheService;
import com.gdufe.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查：一次请求同时验证 Web 层、MySQL 连接、缓存实现与时间序列化。
 * 访问：GET /api/health（无需登录）
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final CacheService cacheService;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", "campus-mall");
        data.put("time", LocalDateTime.now());

        try (Connection conn = dataSource.getConnection()) {
            var meta = conn.getMetaData();
            data.put("dbOk", true);
            data.put("dbProduct", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            data.put("dbSchema", conn.getCatalog());
        } catch (Exception e) {
            data.put("dbOk", false);
            data.put("dbError", e.getMessage());
        }

        try {
            cacheService.set("health:probe", "1", 30);
            data.put("cacheType", cacheService.type());
            data.put("cacheOk", cacheService.ping() && "1".equals(cacheService.get("health:probe")));
        } catch (Exception e) {
            data.put("cacheOk", false);
            data.put("cacheError", e.getMessage());
        }
        return Result.ok(data);
    }
}
