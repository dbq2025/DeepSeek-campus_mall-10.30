package com.gdufe.mall.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回体，字段名与前端约定一致，禁止改名。
 * 注意：分页元数据必须是 JSON number。全局 Jackson 把 Long 序列化成字符串（保护雪花 ID），
 * 若这里用 long，total/current/size/pages 会变成 "10"，前端 el-pagination 无法正确计算。
 * 因此这里统一用 Integer，不参与 Long->String 的全局规则。
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private Integer total;
    private Integer current;
    private Integer size;
    private Integer pages;

    /**
     * 分页元数据来自数据库分页对象，records 由调用方按 VO 组装后 setRecords 覆盖，
     * 因此这里对 records 做一次受控的窄化转换（避免每个 service 都写一遍样板强转）。
     */
    @SuppressWarnings("unchecked")
    public static <T> PageResult<T> of(IPage<?> page) {
        PageResult<T> pr = new PageResult<>();
        pr.records = (List<T>) page.getRecords();
        pr.total = (int) page.getTotal();
        pr.current = (int) page.getCurrent();
        pr.size = (int) page.getSize();
        pr.pages = (int) page.getPages();
        return pr;
    }
}
