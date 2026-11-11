package com.liucai.permission.core;

import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/5
 */
public interface LcaiPermissionActivityResult {

    void onPermissionResult(boolean granted, Map<String,Boolean> permissions);
}
