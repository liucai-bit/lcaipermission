package com.liucai.lcaidb.base;

import com.liucai.core.util.lcaidb.LcaiAspect;
import com.liucai.lcaidb.base.sql.LcaiSql;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public abstract class BaseCRUD<T> implements LcaiSql {

    public Class aClass;

    public T entity;

    public BaseCRUD(Class<?> aClass){
        this.aClass = aClass;
        try {
            entity = (T) aClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取注解表名
     * @param aClass
     * @return
     */
    public String getTableName(Class<?> aClass) {
        return LcaiAspect.getTableNameValue(aClass);
    }

    /**
     * 获取注解表主键
     * @param aClass
     * @return
     */
    public String getTableId(Class<?> aClass) {
        return LcaiAspect.getTableIdValue(aClass);
    }
}
