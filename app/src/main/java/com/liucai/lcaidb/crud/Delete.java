package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * DELETE 语句生成器。
 */
public class Delete<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Delete(Class<?> aClass) {
        super(aClass);
    }

    public String deleteById(Collection<Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Serializable s : idList) {
            if (s == null) continue;
            if (sb.length() > 0) sb.append(",");
            // 使用 wrap 安全包裹
            sb.append(wrap(s));
        }
        if (sb.length() == 0) return "";
        return format(DELETE_BYID,
                getTableName(entity.getClass()),
                getTableId(entity.getClass()),
                sb.toString());
    }

    public String deleteByParams(Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (!first) builder.append(SPACE).append(AND).append(SPACE);
            // 修复: 增加驼峰转下划线，与 Query/Update 保持一致
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            builder.append(wrap(value));
            first = false;
        }
        if (builder.length() == 0) return "";
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());
    }

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
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            builder.append(wrap(value));
            first = false;
        }
        if (builder.length() == 0) return "";
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());
    }
}