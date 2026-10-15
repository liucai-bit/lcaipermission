package com.liucai.lcaidb.page;

import com.liucai.lcaidb.base.module.LcaiTableInfo;

/**
 * 分页参数接口。约定 current 从 1 开始。
 *
 * @author liucai
 */
public interface ILcaiPage<T extends LcaiTableInfo> {

    /**
     * 每页条数。
     */
    long getSize();

    /**
     * 当前页码，从 1 开始。
     */
    long getCurrent();

    /**
     * 当前页首条记录的偏移量（0-based）。
     */
    default long getStart() {
        long current = Math.max(getCurrent(), 1);
        return (current - 1) * getSize();
    }

    /**
     * 当前页条数（即 getSize）。
     */
    default long getEnd() {
        return getSize();
    }
}