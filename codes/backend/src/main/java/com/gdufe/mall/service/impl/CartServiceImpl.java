package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.SecurityUtil;
import com.gdufe.mall.common.util.SpecUtil;
import com.gdufe.mall.dto.CartAddDTO;
import com.gdufe.mall.dto.CartUpdateDTO;
import com.gdufe.mall.entity.Cart;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CartMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.CartService;
import com.gdufe.mall.vo.CartItemVO;
import com.gdufe.mall.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Override
    public CartVO list() {
        Long userId = SecurityUtil.currentUserId();
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
                .orderByDesc(Cart::getId));

        CartVO vo = new CartVO();
        if (carts.isEmpty()) {
            vo.setItems(new ArrayList<>());
            vo.setTotalQuantity(0);
            vo.setTotalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return vo;
        }

        Map<Long, Product> products = loadProducts(carts);
        List<CartItemVO> items = new ArrayList<>(carts.size());
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart c : carts) {
            Product p = products.get(c.getProductId());
            if (p == null) {
                // 商品被逻辑删除：历史购物车行不再展示（下单时同样会 3003）
                continue;
            }
            CartItemVO item = toVO(c, p);
            items.add(item);
            totalQuantity += c.getQuantity();
            totalAmount = totalAmount.add(p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
        }
        vo.setItems(items);
        vo.setTotalQuantity(totalQuantity);
        vo.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        return vo;
    }

    @Override
    public CartItemVO add(CartAddDTO dto) {
        Long userId = SecurityUtil.currentUserId();
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        if (p.getStatus() == null || p.getStatus() != 1) {
            throw new BizException(ResultCode.PRODUCT_OFF_SHELF);
        }
        // 规格必须合法且归一化，否则同商品同规格会生成多行（唯一键是 user+product+spec_text）
        String specText = SpecUtil.normalize(p.getSpecJson(), dto.getSpecText());

        Cart exist = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, p.getId())
                .eq(Cart::getSpecText, specText));

        if (exist != null) {
            // 同商品同规格累加，不新增行（契约边界 #14）
            exist.setQuantity(exist.getQuantity() + dto.getQuantity());
            cartMapper.updateById(exist);
            return toVO(exist, p);
        }

        Cart c = new Cart();
        c.setUserId(userId);
        c.setProductId(p.getId());
        c.setQuantity(dto.getQuantity());
        c.setSpecText(specText);
        cartMapper.insert(c);
        return toVO(c, p);
    }

    @Override
    public CartItemVO updateQuantity(Long id, CartUpdateDTO dto) {
        Cart c = requireOwnCart(id);
        Product p = productMapper.selectById(c.getProductId());
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品已下架");
        }
        if (p.getStock() != null && dto.getQuantity() > p.getStock()) {
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH,
                    "库存不足，仅剩 " + p.getStock() + " 件");
        }
        c.setQuantity(dto.getQuantity());
        cartMapper.updateById(c);
        return toVO(c, p);
    }

    @Override
    public void remove(Long id) {
        Cart c = requireOwnCart(id);
        cartMapper.deleteById(c.getId());
    }

    @Override
    public void clear() {
        Long userId = SecurityUtil.currentUserId();
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    /** 行归属校验：别人的行一律当「不存在」，不泄露存在性（契约边界 #16） */
    private Cart requireOwnCart(Long id) {
        Cart c = cartMapper.selectById(id);
        if (c == null || !c.getUserId().equals(SecurityUtil.currentUserId())) {
            throw new BizException(ResultCode.NOT_FOUND, "购物车商品不存在");
        }
        return c;
    }

    private Map<Long, Product> loadProducts(List<Cart> carts) {
        List<Long> ids = carts.stream().map(Cart::getProductId).distinct().toList();
        Map<Long, Product> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        for (Product p : productMapper.selectBatchIds(ids)) {
            map.put(p.getId(), p);
        }
        return map;
    }

    private CartItemVO toVO(Cart c, Product p) {
        CartItemVO vo = new CartItemVO();
        vo.setId(c.getId());
        vo.setProductId(c.getProductId());
        vo.setProductName(p.getName());
        vo.setMainImage(p.getMainImage());
        vo.setPrice(p.getPrice());
        vo.setQuantity(c.getQuantity());
        vo.setSpecText(c.getSpecText() == null ? "" : c.getSpecText());
        vo.setStock(p.getStock());
        vo.setProductStatus(p.getStatus());
        return vo;
    }
}
