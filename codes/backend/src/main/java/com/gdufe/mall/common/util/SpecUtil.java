package com.gdufe.mall.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.ResultCode;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 规格处理（方案 B：不做真 SKU，spec_json 只定义可选项，spec_text 做快照）。
 * 约定格式：
 *   spec_json = [{"name":"颜色","values":["黑","白"]},{"name":"尺码","values":["M","L"]}]
 *   spec_text = "颜色:黑;尺码:M"
 * 校验与归一化规则必须与前端 SpecSelector 保持一致，否则购物车唯一键会分裂。
 */
public final class SpecUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SpecUtil() {
    }

    /** 商品表单校验：spec_json 必须是合法数组，名称/值非空且不重复 */
    public static void validateSpecJson(String specJson) {
        if (isBlank(specJson)) {
            return;
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(specJson);
        } catch (Exception e) {
            throw new BizException(ResultCode.PARAM_ERROR, "规格定义不是合法 JSON");
        }
        if (!root.isArray() || root.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "规格定义必须是非空 JSON 数组");
        }
        Set<String> names = new LinkedHashSet<>();
        for (JsonNode spec : root) {
            String name = spec.path("name").asText("").trim();
            if (name.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格名称不能为空");
            }
            if (!names.add(name)) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格名称重复：" + name);
            }
            JsonNode values = spec.path("values");
            if (!values.isArray() || values.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格「" + name + "」的可选值不能为空");
            }
            Set<String> valueSet = new LinkedHashSet<>();
            for (JsonNode v : values) {
                String value = v.asText("").trim();
                if (value.isEmpty()) {
                    throw new BizException(ResultCode.PARAM_ERROR, "规格「" + name + "」存在空值");
                }
                if (!valueSet.add(value)) {
                    throw new BizException(ResultCode.PARAM_ERROR, "规格「" + name + "」的值重复：" + value);
                }
            }
        }
    }

    /**
     * 把用户选择的 spec_text 归一化为按定义顺序排列的标准串。
     * 无规格商品：必须不带 spec_text，返回空串；
     * 有规格商品：必须每个规格都有合法值，多传未知规格视为非法。
     */
    public static String normalize(String specJson, String specText) {
        Map<String, Set<String>> defs = parseDefs(specJson);
        String text = specText == null ? "" : specText.trim();
        if (defs.isEmpty()) {
            if (!text.isEmpty()) {
                throw new BizException(ResultCode.PARAM_ERROR, "该商品无规格，不能提交规格信息");
            }
            return "";
        }
        if (text.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "请选择完整的商品规格");
        }
        Map<String, String> picked = new LinkedHashMap<>();
        for (String pair : text.split(";")) {
            String item = pair.trim();
            if (item.isEmpty()) {
                continue;
            }
            int idx = item.indexOf(':');
            if (idx <= 0 || idx == item.length() - 1) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格格式不正确，应为 名称:值");
            }
            String name = item.substring(0, idx).trim();
            String value = item.substring(idx + 1).trim();
            if (!defs.containsKey(name)) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格项不存在：" + name);
            }
            if (picked.put(name, value) != null) {
                throw new BizException(ResultCode.PARAM_ERROR, "规格项重复：" + name);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : defs.entrySet()) {
            String value = picked.get(entry.getKey());
            if (value == null) {
                throw new BizException(ResultCode.PARAM_ERROR, "请选择完整的商品规格");
            }
            if (!entry.getValue().contains(value)) {
                throw new BizException(ResultCode.PARAM_ERROR,
                        "规格「" + entry.getKey() + "」不支持取值：" + value);
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(entry.getKey()).append(':').append(value);
        }
        return sb.toString();
    }

    private static Map<String, Set<String>> parseDefs(String specJson) {
        Map<String, Set<String>> defs = new LinkedHashMap<>();
        if (isBlank(specJson)) {
            return defs;
        }
        try {
            JsonNode root = MAPPER.readTree(specJson);
            if (!root.isArray()) {
                return defs;
            }
            for (JsonNode spec : root) {
                String name = spec.path("name").asText("").trim();
                if (name.isEmpty()) {
                    continue;
                }
                Set<String> values = new LinkedHashSet<>();
                for (JsonNode v : spec.path("values")) {
                    values.add(v.asText("").trim());
                }
                defs.put(name, values);
            }
        } catch (Exception e) {
            // 历史脏数据：当作无规格处理，避免整条链路不可用
            return new LinkedHashMap<>();
        }
        return defs;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
