package com.liucai.permission.bean;

import androidx.annotation.NonNull;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/10/14
 */
public class LcaiPermissionEntity {
    /** 权限名，如 Manifest.permission.CAMERA */
    public String permission;
    /** 最近一次是否授权 */
    public boolean granted;
    /** 是否判定为"永久拒绝"（勾了不再询问 / 系统策略） */
    public boolean permanentlyDenied;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public boolean isPermanentlyDenied() {
        return permanentlyDenied;
    }

    public void setPermanentlyDenied(boolean permanentlyDenied) {
        this.permanentlyDenied = permanentlyDenied;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
