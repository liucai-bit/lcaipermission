package com.liucai.lcaidb.crud;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.text.TextUtils;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.enu.OrderTypeEnum;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class Query<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Query(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 获取实体类
     * @param cursor
     * @param aClass
     * @return
     */
    @SuppressLint("Range")
    public T getEntity(Cursor cursor, Class<?> aClass) {
        try {
            entity = (T) aClass.newInstance();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fileName = CamelCaseConverter.toUnderscore(field.getName());
                Object fileValue = null;
                Class<?> type = field.getType();
                if (type.equals(String.class)) {
                    fileValue = cursor.getString(cursor.getColumnIndex(fileName));
                } else if (type.equals(Integer.class)) {
                    fileValue = cursor.getInt(cursor.getColumnIndex(fileName));
                } else if (type.equals(Double.class)) {
                    fileValue = cursor.getDouble(cursor.getColumnIndex(fileName));
                } else if (type.equals(Float.class)) {
                    fileValue = cursor.getFloat(cursor.getColumnIndex(fileName));
                } else if (type.equals(Long.class)) {
                    fileValue = cursor.getLong(cursor.getColumnIndex(fileName));
                } else if (type.equals(Short.class)) {
                    fileValue = cursor.getShort(cursor.getColumnIndex(fileName));
                }
                if (!TextUtils.isEmpty(fileValue+"")) {
                    field.set(entity, fileValue);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            entity = null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            entity = null;
        }
        return (T) entity;
    }

    /**
     * 查询全部
     * @return
     */
    public String queryList() {
        return format(QUERYLIST, getTableName(entity.getClass()));
    }

    /**
     * 按照条件查询
     * @param condition
     * @return
     */
    public String queryByParams(Map<String, Object> condition) {
        String sql = "";
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            if (!TextUtils.isEmpty(sql)) {
                sql += AND + SPACE;
            }
            sql += CamelCaseConverter.toUnderscore(entry.getKey()) + "=" + ((entry.getValue() instanceof String) ? format(STRING,entry.getValue().toString()) : entry.getValue())+ SPACE;
        }
        return format(TABLE_QUERY, getTableName(entity.getClass()),sql);
    }

    /**
     * 分页查询
     * @param start
     * @param end
     * @param order
     * @param typeEnum
     * @return
     */
    public String queryByPage(long start, long end, String order, OrderTypeEnum typeEnum) {
        StringBuilder builder = new StringBuilder();
        builder.append(format(QUERYLIST, getTableName(entity.getClass())));
        builder.append(SPACE);
        builder.append(format(LIMIT_FOR, start + "", end + "")).append(SPACE);
        if (!TextUtils.isEmpty(order)) {
            builder.append(format(ORDER_BY_FOR, order, typeEnum));
        }
        return format(builder.toString());
    }

    /**
     * 分页查询+条件查询
     * @param start
     * @param end
     * @param order
     * @param ordertype
     * @param condition
     * @return
     */
    public String queryByPageParams(long start, long end, String order, OrderTypeEnum ordertype, Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        builder.append(queryByParams(condition));
        builder.append(SPACE);
        builder.append(format(LIMIT_FOR, start + "", end + "")).append(SPACE);
        if (!TextUtils.isEmpty(order)) {
            builder.append(format(ORDER_BY_FOR, order, ordertype));
        }
        return format(builder.toString());
    }

    public String queryCount() {
        return format(SELECT_COUNTS, getTableName(entity.getClass()));
    }
}
