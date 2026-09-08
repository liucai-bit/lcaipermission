package com.liucai.permission.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.view.LcaiPermissionActivity;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBulider;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionRequest {

    public LcaiPermissionRequestBulider builder;

    public LcaiPermissionRequest(LcaiPermissionRequestBulider builder) {
        this.builder = builder;
        LcaiLogUtils.d("request permission start");
        checkSelPermission();
    }

    /**
     * 校验权限是否授权，如果没有授权就去申请权限
     *
     * @return
     */
    public void checkSelPermission() {
        //判断SDK版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//如果是6.0以下，不申请
            LcaiLogUtils.i("SDK_INT<23");
            if (builder.result != null) {
                builder.result.onReqPermissionPass();
            }
            return;
        }
        List<String> stringList = new ArrayList<>();
        Map<String, Boolean> permissions = new ConcurrentHashMap<>();
        //未授权集合
        for (String permission : builder.permissions) {
            if (TextUtils.equals(permission,LcaiPermissionString.NOTIFICATIONS)) {
                //如果是通知权限 同时Android版本在Android 13以下
                LcaiLogUtils.i("含有Android 13以下通知权限申请", "是否拥有通知权限:" + checkNotification());
            }else {
                //其他权限
                if (!checkPermission(permission)) {
                    permissions.put(permission, false);
                    stringList.add(permission);
                }
            }
        }

        if (!stringList.isEmpty() || this.builder.hasNotification) {
            if (builder.checkPermission) {
                //如果只是权限检查，直接返回失败
                if (builder.result != null) {
                    builder.result.onReqPermissionNoPass(permissions);
                }
            }else{
                //如果是需要申请权限
                if (builder.asDialog) {
                    showDialog(stringList, permissions);
                } else {
                    reqPermission(stringList);
                }
            }
        } else {
            if (builder.result != null) {
                builder.result.onReqPermissionPass();
            }
        }
    }

    public boolean checkPermission(String permission) {
        if (TextUtils.isEmpty(permission) || !permission.startsWith("android.permission.")) {
            throw new LcaiHttpException("请传入正常的权限字符串，例如: android.permission.CAMERA");
        }
        int i = ContextCompat.checkSelfPermission(builder.mActivity, permission);
        if (i== PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    /**
     * 检查Android 13 以下通知是否授权
     * @return
     */
    public boolean checkNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) this.builder.mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            return (manager != null && manager.areNotificationsEnabled());
        }
        //Android 8以下没有通知开关
        return true;
    }

    public void showDialog(List<String> stringList,Map<String,Boolean> permissions) {
        new LcaiTipsDialogBulider()
                .with(builder.mActivity)
                .addTitle(builder.title)
                .addTitleColor(builder.titleColor)
                .addTitleSize(builder.titleSize)
                .addContent(builder.content)
                .addContentColor(builder.contentColor)
                .addContentSize(builder.contentSize)
                .addCancelText(builder.leftString)
                .addCancelSize(builder.btnSize)
                .addCancelColor(builder.leftColor)
                .addCancelBackground(builder.leftBg)
                .addConfirmText(builder.rightString)
                .addConfirmColor(builder.rightColor)
                .addConfirmSize(builder.btnSize)
                .addConfirmBackground(builder.rightBg)
                .addTipsBackground(builder.tipsBackground)
                .addDialogInterface(new OnTipsDialogInterface() {
                    @Override
                    public void onCancelListener() {
                        if (builder.result != null) {
                            builder.result.onReqPermissionNoPass(permissions);
                        }
                    }

                    @Override
                    public void onConfirmListener() {
                        reqPermission(stringList);
                    }
                }).bulid();

    }

    public void reqPermission(List<String> stringList) {
        LcaiLogUtils.i(stringList.size(),"start request permission");
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArray(LcaiPermissionString.PERMISSION_KEY, stringList.toArray(new String[stringList.size()]));
        bundle.putBoolean(LcaiPermissionString.HAS_NOTIFICATION,this.builder.hasNotification);
        intent.putExtras(bundle);
        intent.setClass(builder.mActivity, LcaiPermissionActivity.class);
        builder.mActivity.startActivity(intent);
    }
}

