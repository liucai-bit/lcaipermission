package com.liucai.lcaidb.core;

import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.enu.OrderTypeEnum;
import com.liucai.lcaidb.helper.LcaiDbResult;
import com.liucai.lcaidb.page.LcaiPage;
import com.liucai.lcaidb.services.impl.LcaidbServiceImpl;
import com.liucai.lcaidb.services.service.LcaidbService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 数据库门面类，对外暴露 CRUD 方法。
 *
 * @author liucai
 */
public class LcaidbCore<T extends LcaiTableInfo> extends BaseCRUD<T> {

    private final LcaidbService<T> service;

    public LcaidbCore(Class<T> aClass) {
        super(aClass);
        this.service = new LcaidbServiceImpl<>(aClass);
    }

    public void createTableForClass() {
        service.createTableForClass();
    }

    // ---------------- 插入 ----------------

    public boolean insert(T entity) {
        return LcaiDbResult.retBool(service.insert(entity));
    }

    public boolean batchInsert(List<T> entities) {
        return LcaiDbResult.retBool(service.batchInsert(entities));
    }

    // ---------------- 删除 ----------------

    public boolean deleteById(Collection<Serializable> idList) {
        return service.deleteById(idList);
    }

    public boolean deleteByParams(Map<String, Object> condition) {
        return service.deleteByParams(condition);
    }

    public boolean deleteByEntity(T entity) {
        return service.deleteByEntity(entity);
    }

    // ---------------- 更新 ----------------

    public boolean updateById(T entity) {
        return service.updateById(entity);
    }

    public boolean updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        return service.updateByParams(condition, update);
    }

    // ---------------- 查询 ----------------

    public List<T> query() {
        return service.query();
    }

    public T queryById(T entity) {
        return service.queryById(entity);
    }

    public List<T> queryByParams(Map<String, Object> condition) {
        return service.queryParams(condition);
    }

    public List<T> queryPage(long size, long current, String order, OrderTypeEnum orderType) {
        LcaiPage<T> page = new LcaiPage<>(entity, size, current);
        return service.queryByPage(page, order, orderType);
    }

    public List<T> queryPageByParams(long size, long current, String order,
                                     OrderTypeEnum orderType, Map<String, Object> condition) {
        LcaiPage<T> page = new LcaiPage<>(entity, size, current);
        return service.queryByPageParams(page, order, orderType, condition);
    }

    public long queryCount() {
        return service.queryCount();
    }
}