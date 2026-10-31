package com.gdufe.mall.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 上传文件的静态映射：数据库只存相对路径，访问走 /api/files/**。
 * 两个位置：先找运行期上传目录（可写），再找 classpath 内的演示图片（随仓库分发，保证克隆后图片不丢）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${mall.upload.dir:./upload}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String abs = Paths.get(uploadDir).toAbsolutePath().normalize().toString().replace("\\", "/");
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + abs + "/", "classpath:/static/files/");
    }
}
