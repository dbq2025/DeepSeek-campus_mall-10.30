package com.gdufe.mall.service;

import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.dto.CategoryForm;
import com.gdufe.mall.vo.CategoryItemVO;
import com.gdufe.mall.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /** 下拉用，按 sort 升序；走缓存 */
    List<CategoryItemVO> listOptions();

    PageResult<CategoryVO> page(String keyword, Integer page, Integer size);

    void create(CategoryForm form);

    void update(Long id, CategoryForm form);

    void delete(Long id);
}
