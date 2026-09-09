package com.liucai.permission.bulider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;

import com.liucai.permission.core.LcaiPermissionString;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionRequestBulider {

    /**
     * 上下文对象
     */
    public Activity mActivity;

    /**
     * 检查权限
     */
    public boolean checkPermission;

    /**
     * 是否包含通知权限
     */
    public boolean hasNotification;

    /**
     * 是否显示自定义索权弹窗
     * 默认不显示
     */
    public boolean asDialog = false;
    /**
     * 自定义索取按弹窗
     */
    public LcaiTipsDialogBuilder builder;
    /**
     * 权限申请标题
     */
    public String title;

    /**
     * 权限申请内容
     */
    public String content;

    /**
     * 拒绝后显示内容
     */
    public String neverContent;

    /**
     * 左侧按钮内容
     */
    public String leftString;

    /**
     * 左侧按钮背景
     * 默认无背景
     */
    public int leftBg;

    /**
     * 按钮文字大小
     * 默认16
     */
    public int btnSize = 16;

    /**
     * 左侧文字颜色
     * 默认#66676B
     */
    public int leftColor = Color.parseColor("#66676B");

    /**
     * 右侧按钮内容
     */
    public String rightString;

    /**
     * 右侧文字背景
     * 默认无背景
     */
    public int rightBg;

    /**
     * 右侧文字颜色
     * 默认#FF9800
     */
    public int rightColor = Color.parseColor("#FF9800");

    /**
     * 内容文字颜色
     * 默认#66676B
     */
    public int contentColor = Color.parseColor("#66676B");

    /**
     * 内容文字大小
     * 默认14
     */
    public int contentSize = 14;

    /**
     * 标题文字颜色
     * 默认#333333
     */
    public int titleColor = Color.parseColor("#333333");

    /**
     * 标题文字大小
     * 默认18
     */
    public int titleSize = 18;

    /**
     * 弹窗背景颜色
     */
    public int tipsBackground;


    /**
     * 权限集合
     */
    public List<String> permissions;

    /**
     * 是否跳转系统设置
     */
    public boolean system;
    /**
     * 跳转系统设置弹窗
     */
    public LcaiTipsDialogBuilder toSystemBuilder;
    /**
     * 申请回调
     */
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

    public LcaiPermissionRequestBulider addDialogBulider(LcaiTipsDialogBuilder builder) {
        this.builder = builder;
        return this;
    }

    /**
     * 索权弹窗标题
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 索权弹窗内容
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 索权弹窗拒绝时显示内容
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addContentNever(String neverContent) {
        this.neverContent = neverContent;
        return this;
    }

    /**
     * 索权弹窗内容文字大小
     * 调用showDialog时设置
     * 默认 14
     */
    public LcaiPermissionRequestBulider addContentSize(int contentSize) {
        this.contentSize = contentSize;
        return this;
    }

    /**
     * 索权弹窗内容文字颜色
     * 调用showDialog时设置
     * 默认#66676B
     */
    public LcaiPermissionRequestBulider addContentColor(int contentColor) {
        this.contentColor = contentColor;
        return this;
    }

    /**
     * 索权弹窗左侧按钮
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addLeftString(String leftString) {
        this.leftString = leftString;
        return this;
    }

    /**
     * 索权弹窗左侧按钮背景
     * 调用showDialog时设置
     * 默认无背景
     */
    public LcaiPermissionRequestBulider addLeftBg(int leftBg) {
        this.leftBg = leftBg;
        return this;
    }

    /**
     * 索权弹窗按钮文字大小
     * 调用showDialog时设置
     * 默认 16
     */
    public LcaiPermissionRequestBulider addBtnSize(int btnSize) {
        this.btnSize = btnSize;
        return this;
    }

    /**
     * 索权弹窗左侧按钮颜色
     * 调用showDialog时设置
     * 默认 #66676B
     */
    public LcaiPermissionRequestBulider addLeftColor(int leftColor) {
        this.leftColor = leftColor;
        return this;
    }

    /**
     * 索权弹窗右侧按钮文字
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addRightStirng(String rightString) {
        this.rightString = rightString;
        return this;
    }

    /**
     * 索权弹窗右侧按钮颜色
     * 调用showDialog时设置
     */
    public LcaiPermissionRequestBulider addRightColor(int rightColor) {
        this.rightColor = rightColor;
        return this;
    }

    /**
     * 索权弹窗右侧按钮背景
     * 调用showDialog时设置
     * 默认无背景
     */
    public LcaiPermissionRequestBulider addRightBg(int rightBg) {
        this.rightBg = rightBg;
        return this;
    }

    /**
     * 索权弹窗标题文字颜色
     * 调用showDialog时设置
     * 默认 #333333
     */
    public LcaiPermissionRequestBulider addTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    /**
     * 索权弹窗标题文字大小
     * 调用showDialog时设置
     * 默认 18
     */
    public LcaiPermissionRequestBulider addTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public LcaiPermissionRequestBulider addTipsBackground(int tipsBackground) {
        this.tipsBackground = tipsBackground;
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
        for (String permission : pers) {
            permissions.add(permission);
        }
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
