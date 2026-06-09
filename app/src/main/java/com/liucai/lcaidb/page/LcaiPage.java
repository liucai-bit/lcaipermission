package com.liucai.lcaidb.page;

import com.liucai.lcaidb.base.module.LcaiTableInfo;
/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class LcaiPage<T extends LcaiTableInfo> implements ILcaiPage<T>{

    public T entity;

    /**
     * 一页多少条
     */
    public long size;

    /**
     * 当前多少页
     */
    public long curren;

    public LcaiPage(T entity, long size, long curren) {
        this.entity = entity;
        this.size = size;
        this.curren = curren;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCurrent() {
        return curren;
    }

    public T getEntity() {
        return entity;
    }
}
