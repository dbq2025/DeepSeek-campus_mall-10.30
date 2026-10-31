package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdufe.mall.common.cache.CacheService;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.dto.CategoryForm;
import com.gdufe.mall.entity.Category;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.CategoryMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.CategoryService;
import com.gdufe.mall.vo.CategoryItemVO;
import com.gdufe.mall.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CACHE_KEY = "mall:category:options";
    private static final long CACHE_TTL = 300L;

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryItemVO> listOptions() {
        // 缓存读写异常一律降级查库：缓存挂掉不能把接口带崩
        try {
            String cached = cacheService.get(CACHE_KEY);
            if (StringUtils.hasText(cached)) {
                return objectMapper.readValue(cached, new TypeReference<List<CategoryItemVO>>() {
                });
            }
        } catch (Exception e) {
            log.warn("分类缓存读取失败，降级查库：{}", e.getMessage());
        }

        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort).orderByAsc(Category::getId));
        List<CategoryItemVO> vos = new ArrayList<>(list.size());
        for (Category c : list) {
            CategoryItemVO vo = new CategoryItemVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            vos.add(vo);
        }

        try {
            cacheService.set(CACHE_KEY, objectMapper.writeValueAsString(vos), CACHE_TTL);
        } catch (Exception e) {
            log.warn("分类缓存写入失败：{}", e.getMessage());
        }
        return vos;
    }

    @Override
    public PageResult<CategoryVO> page(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(Category::getName, keyword.trim());
        }
        w.orderByAsc(Category::getSort).orderByAsc(Category::getId);

        Page<Category> result = categoryMapper.selectPage(PageUtil.of(page, size), w);
        List<CategoryVO> records = new ArrayList<>(result.getRecords().size());
        for (Category c : result.getRecords()) {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            vo.setCreateTime(c.getCreateTime());
            // 引用商品数决定删除按钮是否可点，逐条 count 在单页 10 条量级可接受
            Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, c.getId()));
            vo.setProductCount(count == null ? 0 : count.intValue());
            records.add(vo);
        }
        PageResult<CategoryVO> pr = PageResult.of(result);
        pr.setRecords(records);
        return pr;
    }

    @Override
    public void create(CategoryForm form) {
        String name = form.getName().trim();
        checkNameUnique(name, null);
        Category c = new Category();
        c.setName(name);
        c.setSort(form.getSort() == null ? 0 : form.getSort());
        categoryMapper.insert(c);
        evictCache();
    }

    @Override
    public void update(Long id, CategoryForm form) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BizException(ResultCode.NOT_FOUND, "类型不存在");
        }
        String name = form.getName().trim();
        checkNameUnique(name, id);
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setSort(form.getSort() == null ? 0 : form.getSort());
        categoryMapper.updateById(c);
        evictCache();
    }

    @Override
    public void delete(Long id) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BizException(ResultCode.NOT_FOUND, "类型不存在");
        }
        Long refCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (refCount != null && refCount > 0) {
            throw new BizException(ResultCode.STATUS_NOT_ALLOWED, "该类型下还有商品，无法删除");
        }
        categoryMapper.deleteById(id);
        evictCache();
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name);
        if (excludeId != null) {
            w.ne(Category::getId, excludeId);
        }
        Long count = categoryMapper.selectCount(w);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.DUPLICATE_OPERATION, "类型名称已存在");
        }
    }

    private void evictCache() {
        try {
            cacheService.delete(CACHE_KEY);
        } catch (Exception e) {
            log.warn("分类缓存清理失败：{}", e.getMessage());
        }
    }
}
