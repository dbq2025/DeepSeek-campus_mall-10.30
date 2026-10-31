package com.gdufe.mall.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 销售趋势：dates 与 amounts 一一对应，长度必须等于请求天数 */
@Data
public class SalesTrendVO {
    private List<String> dates = new ArrayList<>();
    private List<String> amounts = new ArrayList<>();
}
