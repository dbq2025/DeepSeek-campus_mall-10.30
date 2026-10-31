package com.gdufe.mall.service.impl;

import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 2L * 1024 * 1024;
    private static final DateTimeFormatter SUB_DIR = DateTimeFormatter.ofPattern("yyyy/MM");

    @Value("${mall.upload.dir:./upload}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(ResultCode.PARAM_ERROR, "图片不能超过 2MB");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') > 0) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException(ResultCode.PARAM_ERROR, "只支持 jpg/jpeg/png/webp 格式的图片");
        }

        String sub = LocalDate.now().format(SUB_DIR);
        // 文件名完全由服务端生成，不采用客户端文件名，杜绝路径穿越
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(sub);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toFile());
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "图片保存失败，请稍后重试");
        }
        String url = "/files/" + sub + "/" + fileName;
        log.info("图片已上传：{}", url);
        return url;
    }
}
