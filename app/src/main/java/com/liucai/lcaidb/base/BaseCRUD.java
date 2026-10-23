package com.liucai.lcaidb.base;

import com.liucai.core.util.lcaidb.LcaiAspect;
import com.liucai.lcaidb.base.sql.LcaiSql;

/**
 * 所有 CRUD 操作类的基类。
 *
 * @author liucai
 */
public abstract class BaseCRUD<T> implements LcaiSql {

    protected final Class<T> aClass;
    protected T entity;

    @SuppressWarnings("unchecked")
    public BaseCRUD(Class<?> aClass) {
        this.aClass = (Class<T>) aClass;
        try {
            this.entity = this.aClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot instantiate entity: " + aClass.getName()
                            + ". Ensure it has a public no-arg constructor.", e);
        }
    }

    public String getTableName(Class<?> aClass) {
        return LcaiAspect.getTableNameValue(aClass);
    }

    public String getTableId(Class<?> aClass) {
        return LcaiAspect.getTableIdValue(aClass);
    }

    /**
     * 【新增】安全包裹 SQL 值。
     * 自动处理 null、数字、布尔值(转0/1)以及转义字符串单引号，防止 SQL 注入和语法崩溃。
     */
    protected String wrap(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        // 字符串转义：将单引号替换为两个单引号
        String str = value.toString();
        str = str.replace("'", "''");
        return format(STRING, str);
    }
}