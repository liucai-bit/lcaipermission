package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * UPDATE 语句生成器。
 */
public class Update<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Update(Class<?> aClass) {
        super(aClass);
    }

    public String updateById(T entity) {
        if (entity == null) return "";

        Map<String, Object> entityMap = new DBUtils<>().getEntityMap(entity);
        String rawTableId = getTableId(aClass);
        String camelId = CamelCaseConverter.underscoreToCamel(rawTableId);

        String idFieldName = entityMap.containsKey(camelId) ? camelId : rawTableId;
        Object idValue = entityMap.get(idFieldName);
        if (idValue == null || TextUtils.isEmpty(String.valueOf(idValue))) {
            return "";
        }

        StringBuilder setBuilder = new StringBuilder();
        for (Field field : aClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String name = field.getName();
            if (name.equals(idFieldName)) continue;

            Object value = entityMap.get(name);
            if (value == null) continue;
            // 注意：如果非要更新空字符串，不要拦截，允许空字符串赋值
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) {
                // 若是空字符串，这里保持原有跳过逻辑，若业务需要清空，可去掉这行
                continue;
            }

            if (setBuilder.length() > 0) setBuilder.append(",");
            setBuilder.append(CamelCaseConverter.toUnderscore(name)).append("=");
            setBuilder.append(wrap(value));
        }
        if (setBuilder.length() == 0) return "";

        String whereColumn = CamelCaseConverter.toUnderscore(idFieldName);
        return format(UPDATE_BY_ID,
                getTableName(entity.getClass()),
                setBuilder.toString(),
                whereColumn,
                wrap(idValue));
    }

    public String updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        String setClause = buildAssignment(update, ",");
        String whereClause = buildAssignment(condition, SPACE + AND + SPACE);
        if (setClause.isEmpty() || whereClause.isEmpty()) return "";
        return format(UPDATE_BY_PARAMS,
                getTableName(entity.getClass()),
                setClause,
                whereClause);
    }

    private String buildAssignment(Map<String, Object> map, String separator) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (builder.length() > 0) builder.append(separator);
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            builder.append(wrap(value));
        }
        return builder.toString();
    }
}