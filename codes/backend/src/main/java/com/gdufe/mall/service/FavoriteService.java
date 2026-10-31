package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.vo.FavoriteItemVO;

public interface FavoriteService {

    PageResult<FavoriteItemVO> page(Integer page, Integer size);

    void add(Long productId);

    void remove(Long productId);

    boolean check(Long productId);
}
