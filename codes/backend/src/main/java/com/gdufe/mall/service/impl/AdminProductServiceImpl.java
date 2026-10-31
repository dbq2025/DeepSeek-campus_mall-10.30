package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.common.util.SpecUtil;
import com.gdufe.mall.dto.ProductForm;
import com.gdufe.mall.entity.Category;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CategoryMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.AdminProductService;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ProductVO> page(String keyword, Long categoryId, Integer status,
                                      Integer page, Integer size) {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(q -> q.like(Product::getName, kw).or().like(Product::getSubtitle, kw));
        }
        if (categoryId != null) {
            w.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            w.eq(Product::getStatus, status);
        }
        w.orderByDesc(Product::getCreateTime).orderByDesc(Product::getId);

        Page<Product> result = productMapper.selectPage(PageUtil.of(page, size), w);
        Map<Long, String> names = ProductAssembler.categoryNames(
                result.getRecords().stream().map(Product::getCategoryId).toList(), categoryMapper);
        List<ProductVO> records = new ArrayList<>(result.getRecords().size());
        for (Product p : result.getRecords()) {
            records.add(ProductAssembler.toAdminItem(p, names.get(p.getCategoryId())));
        }
        PageResult<ProductVO> pr = PageResult.of(result);
        pr.setRecords(records);
        return pr;
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        Map<Long, String> names = ProductAssembler.categoryNames(List.of(p.getCategoryId()), categoryMapper);
        // 后台详情不做收藏态判断，固定 false
        return ProductAssembler.toDetail(p, names.get(p.getCategoryId()), false);
    }

    @Override
    public Long create(ProductForm form) {
        checkCategory(form.getCategoryId());
        SpecUtil.validateSpecJson(form.getSpecJson());

        Product p = new Product();
        applyForm(p, form);
        p.setSales(0);
        productMapper.insert(p);
        return p.getId();
    }

    @Override
    public void update(Long id, ProductForm form) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        checkCategory(form.getCategoryId());
        SpecUtil.validateSpecJson(form.getSpecJson());

        Product p = new Product();
        p.setId(id);
        applyForm(p, form);
        productMapper.updateById(p);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        Product p = new Product();
        p.setId(id);
        p.setStatus(status);
        productMapper.updateById(p);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "请选择要操作的商品");
        }
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .in(Product::getId, ids)
                .set(Product::getStatus, status));
    }

    @Override
    public void delete(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        // 逻辑删除：历史订单明细是快照，不受影响（契约边界 #28）
        productMapper.deleteById(id);
    }

    private void checkCategory(Long categoryId) {
        Category c = categoryMapper.selectById(categoryId);
        if (c == null) {
            throw new BizException(ResultCode.NOT_FOUND, "所选商品类型不存在");
        }
    }

    private void applyForm(Product p, ProductForm form) {
        p.setCategoryId(form.getCategoryId());
        p.setName(form.getName().trim());
        p.setSubtitle(form.getSubtitle());
        p.setMainImage(form.getMainImage());
        p.setPrice(form.getPrice());
        p.setStock(form.getStock());
        p.setSpecJson(StringUtils.hasText(form.getSpecJson()) ? form.getSpecJson() : null);
        p.setDetail(form.getDetail());
        p.setStatus(form.getStatus());
    }
}
