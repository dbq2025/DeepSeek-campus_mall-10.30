package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/admin/files")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileService fileService;

    /** FILE-01 图片上传，字段名固定 file */
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(Map.of("url", fileService.upload(file)));
    }
}
