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

import java.util.ArrayList;
import java.util.List;

/**
 * @author LIUCAI
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

        if (TextUtils.haveOneArray(bulider.permissions.toArray(new String[bulider.permissions.size()]), LcaiPermissionString.MANAGE_EXTERNAL_STORAGE)) {
            if (!checkPermission(LcaiPermissionString.MANAGE_EXTERNAL_STORAGE)) {
//                requestManageExternalStoragePermission();
                return;
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
            LcaiLogUtils.i(stringList.size(),"start request permission");
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putStringArray(LcaiPermissionString.PERMISSION_KEY, stringList.toArray(new String[stringList.size()]));
            intent.putExtras(bundle);
            intent.setClass(bulider.mActivity, LcaiPermissionActivity.class);
            bulider.mActivity.startActivity(intent);

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

    public void requestManageExternalStoragePermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:"+bulider.mActivity.getPackageName()));
        bulider.mActivity.startActivityForResult(intent, LcaiPermissionActivity.MANAGE_EXTERNAL_PERMISSION);
    }
}

