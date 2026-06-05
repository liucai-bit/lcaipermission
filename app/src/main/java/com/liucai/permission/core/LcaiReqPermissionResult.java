package com.liucai.permission.core;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public interface LcaiReqPermissionResult {
    void onReqPermissionPass();

    default void onReqPermissionNoPass() {

    }
}
