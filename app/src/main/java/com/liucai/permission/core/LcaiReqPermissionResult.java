package com.liucai.permission.core;

import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description 权限申请回调
 * @Date 2026/5/26
 */
public interface LcaiReqPermissionResult {
    /***
     * 所有权限通过
     */
    void onReqPermissionPass();

    /**
     * 有权限没有通过申请
     * @param permissions 未通过申请权限
     * Map<权限名称,是否永久拒绝>
     */
    default void onReqPermissionNoPass(Map<String,Boolean> permissions) {

    }
}
