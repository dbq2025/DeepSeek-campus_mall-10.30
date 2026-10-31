package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.FavoriteDTO;
import com.gdufe.mall.service.FavoriteService;
import com.gdufe.mall.vo.FavoriteItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/front/favorites")
@RequiredArgsConstructor
public class FrontFavoriteController {

    private final FavoriteService favoriteService;

    /** FAV-01 收藏分页 */
    @GetMapping
    public Result<PageResult<FavoriteItemVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.ok(favoriteService.page(page, size));
    }

    /** FAV-02 添加收藏 */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody FavoriteDTO dto) {
        favoriteService.add(dto.getProductId());
        return Result.ok();
    }

    /** FAV-03 取消收藏 */
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(productId);
        return Result.ok();
    }

    /** FAV-04 收藏状态 */
    @GetMapping("/check")
    public Result<Map<String, Boolean>> check(@RequestParam Long productId) {
        return Result.ok(Map.of("favorited", favoriteService.check(productId)));
    }
}
