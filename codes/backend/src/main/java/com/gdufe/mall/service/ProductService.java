package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.vo.ProductDetailVO;
import com.gdufe.mall.vo.ProductItemVO;

/** 前台商品查询（游客可用） */
public interface ProductService {

    PageResult<ProductItemVO> page(String keyword, Long categoryId, String sort, Integer page, Integer size);

    ProductDetailVO detail(Long id);
}
