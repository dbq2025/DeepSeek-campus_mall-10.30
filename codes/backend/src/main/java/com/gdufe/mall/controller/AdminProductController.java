package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.BatchStatusDTO;
import com.gdufe.mall.dto.ProductForm;
import com.gdufe.mall.dto.StatusDTO;
import com.gdufe.mall.service.AdminProductService;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    /** PRD-01 商品分页（含下架） */
    @GetMapping
    public Result<PageResult<ProductVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(adminProductService.page(keyword, categoryId, status, page, size));
    }

    /** PRD-02 商品详情（编辑回填，含下架） */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.ok(adminProductService.detail(id));
    }

    /** PRD-03 新增 */
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody ProductForm form) {
        return Result.ok(Map.of("id", adminProductService.create(form)));
    }

    /** PRD-04 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductForm form) {
        adminProductService.update(id, form);
        return Result.ok();
    }

    /** PRD-05 单个上下架 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        adminProductService.updateStatus(id, dto.getStatus());
        return Result.ok();
    }

    /** PRD-06 批量上下架 */
    @PutMapping("/status")
    public Result<Void> batchStatus(@Valid @RequestBody BatchStatusDTO dto) {
        adminProductService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        return Result.ok();
    }

    /** PRD-07 逻辑删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminProductService.delete(id);
        return Result.ok();
    }
}
