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
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class LcaidbCore<T extends LcaiTableInfo> extends BaseCRUD<T> {

    private LcaidbService lcaidbService;

    public LcaidbCore(Class<?> aClass) {
        super(aClass);
        lcaidbService = new LcaidbServiceImpl(aClass);
    }


    /**
     * 根据实体类创建表
     */
    public void createTableForClass() {
        lcaidbService.createTableForClass();
    }

    /**
     * 插入数据
     * @return
     */
    public boolean insert(T entity) {
        return LcaiDbResult.retBool(lcaidbService.insert(entity));
    }

    /**
     * 批量插入
     * @param entitys
     * @return
     */
    public boolean batchInsert(List<T> entitys) {
        return LcaiDbResult.retBool(lcaidbService.batchInsert(entitys));
    }

    /**
     * 根据ID删除（批量）
     * @param idList id集合
     * @return
     */
    public boolean deleteById(Collection<Serializable> idList) {
        return lcaidbService.deleteById(idList);
    }

    /**
     * 按照条件删除
     * @param condition
     * @return
     */
    public boolean deleteByParams(Map<String,Object> condition) {
        return lcaidbService.deleteByParams(condition);
    }

    /**
     * 按照实体删除
     * @return
     */
    public boolean deleteByEntity(T entity) {
        return lcaidbService.deleteByEntity(entity);
    }

    /**
     * 根据ID 更新数据
     * @return
     */
    public boolean updateById(T entity) {
        return lcaidbService.updateById(entity);
    }

    /**
     * 根据条件更新
     * @param contidion
     * @param update
     * @return
     */
    public boolean updateByParams(Map<String, Object> contidion, Map<String, Object> update) {
        return lcaidbService.updateByParams(contidion, update);
    }

    /**
     * 查询所有数据
     * @return
     */
    public List<T> query() {
        return lcaidbService.query();
    }

    /**
     * 根据ID查询
     * @return
     */
    public T queryById(T entity) {
        return (T) lcaidbService.queryById(entity);
    }

    /**
     * 按照条件查询
     * @param condition
     * @return
     */
    public List<T> queryByParams(Map<String, Object> condition) {
        return lcaidbService.queryParams(condition);
    }

    /**
     * 分页查询
     * @param size
     * @param curren
     * @param order
     * @param ordertype
     * @return
     */
    public List<T> queryPage(long size, long curren, String order, OrderTypeEnum ordertype) {
        LcaiPage<T> page = new LcaiPage<>(entity, size, curren);
        return lcaidbService.qyeryByPage(page, order, ordertype);
    }

    /**
     * 分页条件查询
     * @param size
     * @param curren
     * @param order
     * @param ordertype
     * @param condition
     * @return
     */
    public List<T> queryPageByParams( long size, long curren, String order, OrderTypeEnum ordertype, Map<String, Object> condition) {
        LcaiPage<T> page = new LcaiPage<>(entity, size, curren);
        return lcaidbService.queryByPageParams(page, order, ordertype, condition);
    }

    /**
     * 查询总条数
     * @return
     */
    public long queryCount() {
        return lcaidbService.queryCount();
    }
}
