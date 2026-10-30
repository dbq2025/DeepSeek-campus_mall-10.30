package com.gdufe.mall.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 上传文件的静态映射：数据库只存相对路径，访问走 /api/files/**。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${mall.upload.dir:./upload}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = Paths.get(uploadDir).toAbsolutePath().normalize().toString().replace("\\", "/");
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + abs + "/");
    }
}
