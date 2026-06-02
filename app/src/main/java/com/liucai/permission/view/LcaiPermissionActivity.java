package com.liucai.permission.view;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.liucai.permission.base.LcaiBasePermissionActivity;
import com.liucai.permission.util.LcaiPermissionResult;
import com.liucai.permission.util.LcaiPermissionString;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionActivity extends LcaiBasePermissionActivity {

    public final static int REQUEST_PERMISSION_CODE = 526;

    public final static int MANAGE_EXTERNAL_PERMISSION = 1736;

    private String[] permissionArray;
    private int request_code;

    private static LcaiPermissionResult result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if (savedInstanceState != null) {
            permissionArray = savedInstanceState.getStringArray(LcaiPermissionString.PERMISSION_KEY);
            request_code = savedInstanceState.getInt(LcaiPermissionString.REQUEST_PERMISSION_CODE);
        }

        if (request_code == 0) {
            request_code = REQUEST_PERMISSION_CODE;
        }

        if (permissionArray != null && permissionArray.length > 0) {
            reqPermission();
        }
    }

    private void reqPermission() {
        ActivityCompat.requestPermissions((Activity) this, permissionArray, REQUEST_PERMISSION_CODE);
    }

    public static void setResult(LcaiPermissionResult result) {
        LcaiPermissionActivity.result = result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == request_code) {
            if (result != null) {
                result.onLcaiPermissionResult(requestCode,permissions,grantResults);
            }
        }
        finish();
    }
}
