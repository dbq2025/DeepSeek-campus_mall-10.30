package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdufe.mall.entity.Category;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CategoryMapper;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductItemVO;
import com.gdufe.mall.vo.ProductVO;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品实体到 VO 的转换。放在 impl 包内供前台/后台两个 service 复用，
 * 避免「改名只改一处、另一处漏改」。
 */
final class ProductAssembler {

    private ProductAssembler() {
    }

    /** 批量取分类名，避免列表页 N+1 */
    static Map<Long, String> categoryNames(Collection<Long> categoryIds, CategoryMapper mapper) {
        Set<Long> ids = new HashSet<>(categoryIds);
        ids.remove(null);
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        List<Category> list = mapper.selectList(
                new LambdaQueryWrapper<Category>().in(Category::getId, ids));
        Map<Long, String> map = new HashMap<>();
        for (Category c : list) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }

    static ProductItemVO toItem(Product p, String categoryName) {
        ProductItemVO vo = new ProductItemVO();
        fill(vo, p, categoryName);
        return vo;
    }

    static ProductVO toAdminItem(Product p, String categoryName) {
        ProductVO vo = new ProductVO();
        fill(vo, p, categoryName);
        vo.setStatus(p.getStatus());
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }

    static ProductDetailVO toDetail(Product p, String categoryName, boolean favorited) {
        ProductDetailVO vo = new ProductDetailVO();
        fill(vo, p, categoryName);
        vo.setStatus(p.getStatus());
        vo.setSpecJson(p.getSpecJson());
        vo.setDetail(p.getDetail());
        vo.setCreateTime(p.getCreateTime());
        vo.setFavorited(favorited);
        return vo;
    }

    private static void fill(ProductItemVO vo, Product p, String categoryName) {
        vo.setId(p.getId());
        vo.setName(p.getName());
        vo.setSubtitle(p.getSubtitle());
        vo.setMainImage(p.getMainImage());
        vo.setPrice(p.getPrice());
        vo.setStock(p.getStock());
        vo.setSales(p.getSales());
        vo.setCategoryId(p.getCategoryId());
        vo.setCategoryName(categoryName);
    }
}
