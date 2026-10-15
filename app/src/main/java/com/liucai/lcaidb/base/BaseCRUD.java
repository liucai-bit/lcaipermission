package com.liucai.lcaidb.base;

import com.liucai.core.util.lcaidb.LcaiAspect;
import com.liucai.lcaidb.base.sql.LcaiSql;

/**
 * 所有 CRUD 操作类的基类，持有实体 Class 与实例。
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

    /**
     * 获取实体上 @Table 注解指定的表名。
     */
    public String getTableName(Class<?> aClass) {
        return LcaiAspect.getTableNameValue(aClass);
    }

    /**
     * 获取实体上 @TableId 注解指定的主键列名。
     */
    public String getTableId(Class<?> aClass) {
        return LcaiAspect.getTableIdValue(aClass);
    }
}