package com.gdufe.mall.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /** 保存图片并返回可直接访问的相对路径 /files/yyyy/MM/uuid.ext */
    String upload(MultipartFile file);
}
