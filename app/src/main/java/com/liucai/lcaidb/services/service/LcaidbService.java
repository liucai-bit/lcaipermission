package com.liucai.lcaidb.services.service;

import android.database.sqlite.SQLiteDatabase;

import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.enu.OrderTypeEnum;
import com.liucai.lcaidb.page.LcaiPage;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 数据库 CRUD 服务接口。
 *
 * @author liucai
 */
public interface LcaidbService<T extends LcaiTableInfo> {

    void createTable(String sql);

    void createTableForClass();

    long insert(T entity);

    long batchInsert(List<T> entities);

    boolean deleteById(Collection<Serializable> idList);

    boolean deleteByParams(Map<String, Object> condition);

    boolean deleteByEntity(T entity);

    boolean updateById(T entity);

    boolean updateByParams(Map<String, Object> condition, Map<String, Object> update);

    List<T> query();

    T queryById(T entity);

    List<T> queryParams(Map<String, Object> condition);

    List<T> queryByPage(LcaiPage<T> page, String order, OrderTypeEnum orderType);

    List<T> queryByPageParams(LcaiPage<T> page, String order, OrderTypeEnum orderType,
                              Map<String, Object> condition);

    long queryCount();

    SQLiteDatabase getWriteSqliteDatabase();

    SQLiteDatabase getReadSqliteDatabase();
}