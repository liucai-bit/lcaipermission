package com.liucai.permission.bulider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;

import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionRequestBulider {

    /**上下文对象*/
    public Activity mActivity;

    /**检查权限*/
    public boolean checkPermission;

    /**是否包含通知权限 */
    public boolean hasNotification;

    /**是否显示自定义索权弹窗*/
    public boolean asDialog = false;
    /**自定义索取按弹窗*/
    public LcaiTipsDialogBuilder builder;
    /**权限集合*/
    public List<String> permissions;

    /**是否跳转系统设置*/
    public boolean system;
    /**跳转系统设置弹窗*/
    public LcaiTipsDialogBuilder toSystemBuilder;
    /**申请回调*/
    public LcaiReqPermissionResult result;

    /**
     * 设置上下文对象
     */
    public LcaiPermissionRequestBulider with(Activity mActivity) {
        this.mActivity = mActivity;
        return this;
    }

    /**
     * 仅仅检查权限
     */
    public LcaiPermissionRequestBulider check(boolean checkPermission) {
        this.checkPermission = checkPermission;
        return this;
    }

    /**
     * 添加通知权限
     */
    public LcaiPermissionRequestBulider addNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            //如果是Android 13 以下
            this.hasNotification = true;
            addPermission(LcaiPermissionString.NOTIFICATIONS);
        } else {
            addPermission(LcaiPermissionString.NOTIFICATIONS_13);
        }
        return this;
    }

    /**
     * 需要显示索权弹窗
     */
    public LcaiPermissionRequestBulider showDialog() {
        this.asDialog = true;
        return this;
    }

    /**
     * 添加索取弹窗
     */
    public LcaiPermissionRequestBulider addDialogBulider(LcaiTipsDialogBuilder builder) {
        this.builder = builder;
        return this;
    }
    /**
     * 申请权限
     */
    public LcaiPermissionRequestBulider addPermission(String permission) {
        if (permissions == null || permissions.size() < 1) {
            permissions = new ArrayList<>();
        }
        permissions.add(permission);
        return this;
    }

    /**
     * 一次添加多个权限
     */
    public LcaiPermissionRequestBulider addPermission(String... pers) {
        if (permissions == null) {
            permissions = new ArrayList<>();
        }
        Collections.addAll(permissions, pers);
        return this;
    }

    /**
     * 跳转系统设置
     * 拒绝权限后提示
     */
    public LcaiPermissionRequestBulider toSystem() {
        this.system = true;
        return this;
    }

    /**
     * 跳转系统设置弹窗
     */
    public LcaiPermissionRequestBulider addToSystemBuilder(LcaiTipsDialogBuilder builder) {
        this.toSystemBuilder = builder;
        return this;
    }

    /**
     * 权限回调
     */
    public LcaiPermissionRequestBulider addResult(LcaiReqPermissionResult result) {
        this.result = result;
        return this;
    }


}
