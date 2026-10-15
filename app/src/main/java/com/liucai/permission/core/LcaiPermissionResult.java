package com.liucai.permission.core;

import com.liucai.permission.bean.LcaiPermissionEntity;

import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/10/14
 */
public interface LcaiPermissionResult {
    default void onPermissionsState(PermissionsState state) {

    }

    default void onPermissionRequest(boolean isRequest, List<LcaiPermissionEntity> entities) {

    }
}
