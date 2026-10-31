package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.service.CategoryService;
import com.gdufe.mall.vo.CategoryItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/front/categories")
@RequiredArgsConstructor
public class FrontCategoryController {

    private final CategoryService categoryService;

    /** FPRD-03 类型列表（游客可访问，按 sort 升序） */
    @GetMapping
    public Result<List<CategoryItemVO>> list() {
        return Result.ok(categoryService.listOptions());
    }
}
