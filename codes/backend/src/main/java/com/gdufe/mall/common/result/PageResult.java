package com.gdufe.mall.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回体，字段名与前端约定一致，禁止改名。
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> pr = new PageResult<>();
        pr.records = page.getRecords();
        pr.total = page.getTotal();
        pr.current = page.getCurrent();
        pr.size = page.getSize();
        pr.pages = page.getPages();
        return pr;
    }
}
