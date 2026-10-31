package com.gdufe.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 订单可执行动作，由后端按状态与角色计算，前端只渲染不判断 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionVO {
    /** pay / cancel / confirm / ship */
    private String action;
    private String label;
    /** Element Plus 按钮类型 */
    private String type;
}
