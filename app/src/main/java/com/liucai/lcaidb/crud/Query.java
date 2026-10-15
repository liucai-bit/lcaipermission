package com.liucai.lcaidb.crud;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.enu.OrderTypeEnum;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * SELECT 语句生成器 + Cursor 到实体的映射。
 *
 * @author liucai
 */
public class Query<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Query(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 将 Cursor 当前行映射为实体对象。
     * 注意：调用方负责 cursor 的生命周期与关闭。
     */
    @SuppressLint("Range")
    public T getEntity(Cursor cursor, Class<?> aClass) {
        try {
            T instance = (T) aClass.getDeclaredConstructor().newInstance();
            for (Field field : aClass.getDeclaredFields()) {
                // 跳过 static / transient
                int modifiers = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(modifiers)
                        || java.lang.reflect.Modifier.isTransient(modifiers)) {
                    continue;
                }
                field.setAccessible(true);
                String column = CamelCaseConverter.toUnderscore(field.getName());
                int index = cursor.getColumnIndex(column);
                if (index < 0) continue; // 列不存在跳过

                Class<?> type = field.getType();
                Object value = null;
                if (type.equals(String.class)) {
                    value = cursor.getString(index);
                } else if (type.equals(Integer.class) || type.equals(int.class)) {
                    value = cursor.getInt(index);
                } else if (type.equals(Long.class) || type.equals(long.class)) {
                    value = cursor.getLong(index);
                } else if (type.equals(Double.class) || type.equals(double.class)) {
                    value = cursor.getDouble(index);
                } else if (type.equals(Float.class) || type.equals(float.class)) {
                    value = cursor.getFloat(index);
                } else if (type.equals(Short.class) || type.equals(short.class)) {
                    value = cursor.getShort(index);
                } else if (type.equals(byte[].class)) {
                    value = cursor.getBlob(index);
                }

                // 只处理 null 判断，避免 fileValue+"" == "null" 的误判
                if (value != null) {
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to map cursor to " + aClass.getName(), e);
        }
    }

    public String queryList() {
        return format(QUERYLIST, getTableName(entity.getClass()));
    }

    /**
     * 按条件查询。Map 的 key 为 Java 字段名（转下划线后作为列名），value 为 null 时跳过。
     */
    public String queryByParams(Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;

            if (!first) builder.append(SPACE).append(AND).append(SPACE);
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            if (value instanceof String) {
                builder.append(format(STRING, value));
            } else {
                builder.append(value);
            }
            first = false;
        }
        return format(TABLE_QUERY, getTableName(entity.getClass()), builder.toString());
    }

    public String queryByPage(long start, long end, String order, OrderTypeEnum orderType) {
        StringBuilder builder = new StringBuilder();
        builder.append(format(QUERYLIST, getTableName(entity.getClass())))
                .append(SPACE)
                .append(format(LIMIT_FOR, String.valueOf(start), String.valueOf(end)));
        if (!TextUtils.isEmpty(order) && orderType != null) {
            builder.append(SPACE).append(format(ORDER_BY_FOR, order, orderType.getValue()));
        }
        return builder.toString();
    }

    public String queryByPageParams(long start, long end, String order,
                                    OrderTypeEnum orderType, Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        builder.append(queryByParams(condition))
                .append(SPACE)
                .append(format(LIMIT_FOR, String.valueOf(start), String.valueOf(end)));
        if (!TextUtils.isEmpty(order) && orderType != null) {
            builder.append(SPACE).append(format(ORDER_BY_FOR, order, orderType.getValue()));
        }
        return builder.toString();
    }

    public String queryCount() {
        return format(SELECT_COUNTS, getTableName(entity.getClass()));
    }
}