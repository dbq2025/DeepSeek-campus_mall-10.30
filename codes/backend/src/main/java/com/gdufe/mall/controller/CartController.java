package com.gdufe.mall.controller;

import com.gdufe.mall.common.result.Result;
import com.gdufe.mall.dto.CartAddDTO;
import com.gdufe.mall.dto.CartUpdateDTO;
import com.gdufe.mall.service.CartService;
import com.gdufe.mall.vo.CartItemVO;
import com.gdufe.mall.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/front/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** CART-01 购物车列表 */
    @GetMapping
    public Result<CartVO> list() {
        return Result.ok(cartService.list());
    }

    /** CART-02 加入购物车 */
    @PostMapping
    public Result<CartItemVO> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.ok(cartService.add(dto));
    }

    /** CART-03 修改数量 */
    @PutMapping("/{id}")
    public Result<CartItemVO> update(@PathVariable Long id,
                                     @Valid @RequestBody CartUpdateDTO dto) {
        return Result.ok(cartService.updateQuantity(id, dto));
    }

    /** CART-04 删除一行 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.remove(id);
        return Result.ok();
    }

    /** CART-05 清空购物车 */
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear();
        return Result.ok();
    }
}
