package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.CategoryForm;
import com.gdufe.mall.service.CategoryService;
import com.gdufe.mall.vo.CategoryItemVO;
import com.gdufe.mall.vo.CategoryVO;
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

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /** CAT-01 类型下拉（无分页） */
    @GetMapping
    public Result<List<CategoryItemVO>> options() {
        return Result.ok(categoryService.listOptions());
    }

    /** CAT-02 类型分页 */
    @GetMapping("/page")
    public Result<PageResult<CategoryVO>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(categoryService.page(keyword, page, size));
    }

    /** CAT-03 新增 */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryForm form) {
        categoryService.create(form);
        return Result.ok();
    }

    /** CAT-04 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryForm form) {
        categoryService.update(id, form);
        return Result.ok();
    }

    /** CAT-05 删除（有商品引用时拒绝） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
