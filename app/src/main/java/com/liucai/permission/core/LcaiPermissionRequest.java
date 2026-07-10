package com.liucai.permission.core;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.view.LcaiPermissionActivity;
import com.liucai.tipsdialog.bulider.LcaiTipsDialogBulider;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionRequest {

    public LcaiPermissionRequestBulider bulider;

    public LcaiPermissionRequest(LcaiPermissionRequestBulider bulider) {
        this.bulider = bulider;
        LcaiLogUtils.d("request permission start");
        checkSelPermission();
    }

    /**
     * 校验权限是否授权，如果没有授权就去申请权限
     * @return
     */
    public void checkSelPermission() {
        //判断SDK版本
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//如果是6.0以下，不申请
            LcaiLogUtils.i("SDK_INT<23");
            if (bulider.result != null) {
                bulider.result.onReqPermissionPass();
            }
        }

        List<String> stringList = new ArrayList<>();
        //未授权集合
        for (String permission : bulider.permissions) {
            if (!checkPermission(permission)) {
                stringList.add(permission);
            }
        }

        if (stringList != null && stringList.size() > 0) {
            if (bulider.checkPermission) {
                if (bulider.result != null) {
                    bulider.result.onReqPermissionNoPass();
                }
                return;
            }

            if (bulider.asDialog) {
                showDialog(stringList);
            } else {
                reqPermission(stringList);
            }


        } else {
            if (bulider.result != null) {
                bulider.result.onReqPermissionPass();
            }
        }
    }

    public boolean checkPermission(String permission) {
        //没有授权：PackageManager.PERMISSION_DENIED；已授权：PackageManager.PERMISSION_GRANTED
        int i = ContextCompat.checkSelfPermission(bulider.mActivity, permission);

        if (i== PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public void showDialog(List<String> stringList) {
        new LcaiTipsDialogBulider()
                .with(bulider.mActivity)
                .addTitle(bulider.title)
                .addTitleColor(bulider.titleColor)
                .addTitleSize(bulider.titleSize)
                .addContent(bulider.content)
                .addContentColor(bulider.contentColor)
                .addContentSize(bulider.contentSize)
                .addCancelText(bulider.leftString)
                .addCancelSize(bulider.btnSize)
                .addCancelColor(bulider.leftColor)
                .addCancelColor(bulider.leftColor)
                .addCancelBackground(bulider.leftBg)
                .addConfirmText(bulider.rightString)
                .addConfirmColor(bulider.rightColor)
                .addConfirmSize(bulider.btnSize)
                .addConfirmBackground(bulider.rightBg)
                .addTipsBackground(bulider.tipsBackground)
                .addDialogInterface(new OnTipsDialogInterface() {
                    @Override
                    public void onCancelListener() {
                        if (bulider.result != null) {
                            bulider.result.onReqPermissionPass();
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
        intent.putExtras(bundle);
        intent.setClass(bulider.mActivity, LcaiPermissionActivity.class);
        bulider.mActivity.startActivity(intent);
    }
}

