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
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public interface LcaidbService<T extends LcaiTableInfo> {
    /**
     * 创建表
     * @param sql
     */
    void createTable(String sql);

    /**
     * 根据实体类创建表
     */
    void createTableForClass();


    /**
     * 新增/插入
     */
    long insert(T entity);

    /**
     * 批量插入
     * @param entitys
     * @return
     */
    long batchInsert(List<T> entitys);


    /**
     * 根据ID 主键删除
     */
    boolean deleteById(Collection<Serializable> idList);

    /**
     * 根据 条件删除
     * 表字段
     * Map<表字段,值>
     * DELETE FROM TABLENAME WHERE COLUMN=$s
     */
    boolean deleteByParams(Map<String,Object> condition);

    /**
     * 根据实体删除
     * @return
     */
    boolean deleteByEntity(T entity);

    /**
     * 根据ID 主键修改
     * UPDATE TABLE SET column=%s WHERE TABLEID=ID
     * Map<表字段,值>
     */
    boolean updateById(T entity);

    /**
     * 根据条件 修改
     * UPDATE TABLE SET column=%S WHERE column=%S
     * Map<表字段,值> 条件
     * Map<表字段,值> 更新
     */
    boolean updateByParams(Map<String,Object> condition,Map<String,Object> update);

    /**
     * 查询全部
     * SELECT COLUMN,COLUMN,COLUMN FROM TABLENAME
     */
    List<T> query();

    /**
     * 根据ID 主键查询
     * SELECT COLUMN,COLUMN,COLUMN FROM TABLENAME WHERE TABLEID=ID
     */
    T queryById(T entity);

    /**
     * 根据条件查询
     * SELECT COLUMN,COLUMN FROM TABLENAME WHERE COLUMN=$s
     */
    List<T> queryParams(Map<String,Object> condition);

    /**
     * 分页查询
     * @param page 分页
     * @param order 排序
     * @param ordertype 排序方式
     * SELECT COLUMN FROM TABLENAME LIMIT 0,10
     * @return
     */
    List<T> qyeryByPage(LcaiPage<T> page, String order, OrderTypeEnum ordertype);

    /**
     * 按照条件分页查询
     *
     * @param page
     * @param order
     * @param ordertype
     * @param condition
     * @return
     */
    List<T> queryByPageParams(LcaiPage<T> page, String order, OrderTypeEnum ordertype, Map<String, Object> condition);

    /**
     * 查询总条数
     * @return
     */
    long queryCount();

    /**
     * 获取数据库写入对象
     *
     * @return
     */
    SQLiteDatabase getWriteSqliteDatabase();

    /**
     * 获取数据库读取对象
     * @return
     */
    SQLiteDatabase getReadSqliteDatabase();
}
