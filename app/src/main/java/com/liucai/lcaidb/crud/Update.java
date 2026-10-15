package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * UPDATE 语句生成器。
 *
 * @author liucai
 */
public class Update<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Update(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 按主键更新。主键值从 entity 中提取，其余非空字段作为 SET 内容。
     */
    public String updateById(T entity) {
        if (entity == null) return "";

        Map<String, Object> entityMap = new DBUtils<>().getEntityMap(entity);
        String rawTableId = getTableId(aClass);                        // 注解原值（可能是 id 或 user_id）
        String camelId = CamelCaseConverter.underscoreToCamel(rawTableId); // 驼峰名（userId）

        // 找到实体中的主键字段名
        String idFieldName = entityMap.containsKey(camelId) ? camelId : rawTableId;
        Object idValue = entityMap.get(idFieldName);
        if (idValue == null || TextUtils.isEmpty(String.valueOf(idValue))) {
            return "";
        }

        // SET 子句（跳过主键字段）
        StringBuilder setBuilder = new StringBuilder();
        for (Field field : aClass.getDeclaredFields()) {
            String name = field.getName();
            if (name.equals(idFieldName)) continue;

            Object value = entityMap.get(name);
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (setBuilder.length() > 0) setBuilder.append(",");
            setBuilder.append(CamelCaseConverter.toUnderscore(name)).append("=");
            if (value instanceof String) {
                setBuilder.append(format(STRING, value));
            } else {
                setBuilder.append(value);
            }
        }
        if (setBuilder.length() == 0) return "";

        // WHERE 子句统一用下划线列名
        String whereColumn = CamelCaseConverter.toUnderscore(idFieldName);
        return format(UPDATE_BY_ID,
                getTableName(entity.getClass()),
                setBuilder.toString(),
                whereColumn,
                idValue);
    }

    /**
     * 按条件更新。condition / update 的 key 为 Java 字段名，value 为 null 的项会被跳过。
     */
    public String updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        String setClause = buildAssignment(update, ",");
        String whereClause = buildAssignment(condition, SPACE + AND + SPACE);
        if (setClause.isEmpty() || whereClause.isEmpty()) return "";
        return format(UPDATE_BY_PARAMS,
                getTableName(entity.getClass()),
                setClause,
                whereClause);
    }

    /**
     * 拼接 col1=val1{sep}col2=val2...
     */
    private String buildAssignment(Map<String, Object> map, String separator) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (builder.length() > 0) builder.append(separator);
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            if (value instanceof String) {
                builder.append(format(STRING, value));
            } else {
                builder.append(value);
            }
        }
        return builder.toString();
    }
}