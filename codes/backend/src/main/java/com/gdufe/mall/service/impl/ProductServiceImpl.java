package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.LoginUser;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.entity.Favorite;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CategoryMapper;
import com.gdufe.mall.mapper.FavoriteMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.ProductService;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    public PageResult<ProductItemVO> page(String keyword, Long categoryId, String sort,
                                          Integer page, Integer size) {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(q -> q.like(Product::getName, kw).or().like(Product::getSubtitle, kw));
        }
        if (categoryId != null) {
            w.eq(Product::getCategoryId, categoryId);
        }
        // 排序白名单：绝不能把前端字符串直接拼进 SQL
        String s = sort == null ? "default" : sort.trim();
        switch (s) {
            case "price_asc" -> w.orderByAsc(Product::getPrice);
            case "price_desc" -> w.orderByDesc(Product::getPrice);
            case "sales_desc" -> w.orderByDesc(Product::getSales);
            case "new" -> w.orderByDesc(Product::getCreateTime);
            default -> w.orderByDesc(Product::getSales).orderByDesc(Product::getId);
        }

        Page<Product> result = productMapper.selectPage(PageUtil.of(page, size), w);
        Map<Long, String> names = ProductAssembler.categoryNames(
                result.getRecords().stream().map(Product::getCategoryId).toList(), categoryMapper);
        List<ProductItemVO> records = new ArrayList<>(result.getRecords().size());
        for (Product p : result.getRecords()) {
            records.add(ProductAssembler.toItem(p, names.get(p.getCategoryId())));
        }

        PageResult<ProductItemVO> pr = PageResult.of(result);
        pr.setRecords(records);
        return pr;
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (p.getStatus() == null || p.getStatus() != 1) {
            throw new BizException(ResultCode.PRODUCT_OFF_SHELF);
        }
        Map<Long, String> names = ProductAssembler.categoryNames(List.of(p.getCategoryId()), categoryMapper);
        return ProductAssembler.toDetail(p, names.get(p.getCategoryId()), favorited(id));
    }

    /** 游客访问详情时 favorited 固定 false，不能因为取不到登录态就报错 */
    private boolean favorited(Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser user)) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .eq(Favorite::getProductId, productId));
        return count != null && count > 0;
    }
}
