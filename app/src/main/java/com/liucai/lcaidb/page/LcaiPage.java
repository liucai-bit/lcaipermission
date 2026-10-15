package com.liucai.lcaidb.page;

import com.liucai.lcaidb.base.module.LcaiTableInfo;

/**
 * 分页参数默认实现。
 *
 * @author liucai
 */
public class LcaiPage<T extends LcaiTableInfo> implements ILcaiPage<T> {

    public T entity;
    public long size;
    public long curren; // 保留旧字段名以兼容外部直接字段访问

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