package com.liucai.permission.core;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public interface LcaiReqPermissionResult {
    void OnReqPermissionPass();

    default void OnReqPermissionNoPass() {

    }
}
