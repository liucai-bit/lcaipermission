package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * DELETE 语句生成器。
 *
 * @author liucai
 */
public class Delete<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Delete(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 按主键集合删除。idList 为空时返回空字符串（调用方应处理）。
     */
    public String deleteById(Collection<Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Serializable s : idList) {
            if (s == null) continue;
            if (sb.length() > 0) sb.append(",");
            // 字符串 id 需要加单引号，数字不需要
            if (s instanceof Number || s instanceof Boolean) {
                sb.append(s);
            } else {
                sb.append(format(STRING, s));
            }
        }
        if (sb.length() == 0) return "";
        return format(DELETE_BYID,
                getTableName(entity.getClass()),
                getTableId(entity.getClass()),
                sb.toString());
    }

    /**
     * 按条件 Map 删除。value 为 null 的项会被跳过。
     */
    public String deleteByParams(Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;

            if (!first) builder.append(SPACE).append(AND).append(SPACE);
            builder.append(entry.getKey()).append("=");
            if (value instanceof String) {
                builder.append(format(STRING, value));
            } else {
                builder.append(value);
            }
            first = false;
        }
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());
    }

    /**
     * 按实体非空字段生成删除条件。
     */
    public String deleteByEntity(T entity) {
        if (entity == null) return "";
        StringBuilder builder = new StringBuilder();
        Map<String, Object> fieldMap = new DBUtils<>().getEntityMap(entity);
        boolean first = true;
        for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (!first) builder.append(SPACE).append(AND).append(SPACE);
            // 字段名统一转下划线，和表列名对齐
            builder.append(com.liucai.core.util.lcaidb.CamelCaseConverter
                    .toUnderscore(entry.getKey())).append("=");
            if (value instanceof String) {
                builder.append(format(STRING, value));
            } else {
                builder.append(value);
            }
            first = false;
        }
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());
    }
}