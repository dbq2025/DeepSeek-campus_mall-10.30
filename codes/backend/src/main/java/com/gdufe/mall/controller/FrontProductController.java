package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.service.ProductService;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/front/products")
@RequiredArgsConstructor
public class FrontProductController {

    private final ProductService productService;

    /** FPRD-01 商品分页（游客可访问） */
    @GetMapping
    public Result<PageResult<ProductItemVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(productService.page(keyword, categoryId, sort, page, size));
    }

    /** FPRD-02 商品详情 */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }
}
