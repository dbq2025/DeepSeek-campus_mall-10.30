package com.gdufe.mall.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 分页入参归一化：page<1 -> 1；size<1 -> 10；size>100 -> 100。
 * 非法分页不报错（契约边界 #30），避免前端传参异常导致整页白屏。
 */
public final class PageUtil {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PageUtil() {
    }

    public static <T> Page<T> of(Integer page, Integer size) {
        long current = (page == null || page < 1) ? 1 : page;
        long limit = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new Page<>(current, limit);
    }
}
