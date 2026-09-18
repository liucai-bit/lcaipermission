package com.liucai.component.base;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public interface ItemClickListener{

    void onItemClickListener(int position, Object entity);

    default void loadMore() {

    }

    /**
     * 暂未实现
     */
    default void onRefresh() {

    }
}
