package com.liucai.lcaidb.crud;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.enu.OrderTypeEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * SELECT 语句生成器 + Cursor 到实体的映射。
 */
public class Query<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Query(Class<?> aClass) {
        super(aClass);
    }

    @SuppressLint("Range")
    public T getEntity(Cursor cursor, Class<?> aClass) {
        try {
            T instance = (T) aClass.getDeclaredConstructor().newInstance();
            for (Field field : aClass.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                field.setAccessible(true);
                String column = CamelCaseConverter.toUnderscore(field.getName());
                int index = cursor.getColumnIndex(column);
                if (index < 0) continue;

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
                } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                    value = cursor.getInt(index) == 1; // 修复: 布尔类型映射
                } else if (type.equals(byte[].class)) {
                    value = cursor.getBlob(index);
                }

                if (value != null) {
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to map cursor to " + aClass.getName(), e);
        }
    }

    public String queryList() {
        return format(QUERYLIST, getTableName(entity.getClass()));
    }

    public String queryByParams(Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof CharSequence && TextUtils.isEmpty(value.toString())) continue;

            if (!first) builder.append(SPACE).append(AND).append(SPACE);
            builder.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            builder.append(wrap(value)); // 修复安全包裹
            first = false;
        }
        if (builder.length() == 0) {
            return queryList(); // 如果没有条件，等价于查全部
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