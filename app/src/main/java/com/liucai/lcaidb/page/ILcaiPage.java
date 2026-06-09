package com.liucai.lcaidb.page;


import com.liucai.lcaidb.base.module.LcaiTableInfo;
/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public interface ILcaiPage<T extends LcaiTableInfo> {

    /**
     * 一页显示多少条
     * @return
     */
    long getSize();

    /**
     * 当前页
     * @return
     */
    long getCurrent();

    /**
     * 计算当前页第一条
     * @return
     */
    default long getStart() {
        return (getCurrent() * getSize()) - getSize();
    }

    default long getEnd() {
        return (getCurrent() * getSize());
    }

}
