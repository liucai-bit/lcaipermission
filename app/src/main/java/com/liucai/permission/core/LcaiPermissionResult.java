package com.liucai.permission.core;

import androidx.annotation.NonNull;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public interface LcaiPermissionResult {
    void onLcaiPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
