package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.dto.ProductForm;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductVO;

import java.util.List;

/** 后台商品管理 */
public interface AdminProductService {

    PageResult<ProductVO> page(String keyword, Long categoryId, Integer status, Integer page, Integer size);

    ProductDetailVO detail(Long id);

    Long create(ProductForm form);

    void update(Long id, ProductForm form);

    void updateStatus(Long id, Integer status);

    void batchUpdateStatus(List<Long> ids, Integer status);

    void delete(Long id);
}
