package com.gdufe.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园电商平台后端启动类。
 * 单模块 Maven：分层靠 com.gdufe.mall 下的包结构，不拆子模块。
 * 排除 UserDetailsServiceAutoConfiguration：本项目用 JWT，不需要 Spring 生成的内存用户与随机密码。
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.gdufe.mall.mapper")
@EnableScheduling
public class MallApplication {

    public static void main(String[] args) {
        // 宿主环境（IDE / 沙箱）可能注入 SERVER__PORT 之类的变量，被 Spring 宽松绑定成 server.port，
        // 表现为"启动在 62497 端口且已被占用"。用系统属性固定端口，其优先级高于环境变量；
        // 需要改端口用 -Dserver.port=xxxx 或 MALL_SERVER_PORT。
        if (System.getProperty("server.port") == null) {
            System.setProperty("server.port",
                    System.getenv().getOrDefault("MALL_SERVER_PORT", "8080"));
        }
        SpringApplication.run(MallApplication.class, args);
    }
}
