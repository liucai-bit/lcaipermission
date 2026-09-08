package com.liucai.permission.view;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSONArray;
import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.permission.core.LcaiPermissionString;

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
public class LcaiPermissionActivity extends LcaiBasePermissionActivity {

    private String[] permissionArray;
    private ActivityResultLauncher permissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getIntent().getExtras();
        if (savedInstanceState != null) {
            permissionArray = savedInstanceState.getStringArray(LcaiPermissionString.PERMISSION_KEY);
        }

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean granted = true;
            Map<String, Boolean> permissions = new ConcurrentHashMap<>();
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    granted = false;
                    //是否永久拒绝
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, entry.getKey())) {
                        permissions.put(entry.getKey(),true);
                    } else {
                        permissions.put(entry.getKey(),false);
                    }
                }
            }
            LcaiManager.Internal.getPermissionResult().onPermissionResult(granted,permissions);
            finish();
        });

        if (permissionArray != null && permissionArray.length > 0) {
            permissionLauncher.launch(permissionArray);
        }
    }
}
