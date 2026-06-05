package com.liucai.permission.view;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.liucai.core.LcaiManager;
import com.liucai.core.base.LcaiBasePermissionActivity;
import com.liucai.permission.core.LcaiPermissionString;

/**
 * @author LIUCAI
 * @program lcpermission
 * @description
 * @Date 2026/5/26
 */
public class LcaiPermissionActivity extends LcaiBasePermissionActivity {

    public final static int MANAGE_EXTERNAL_PERMISSION = 1736;

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
            for (boolean b : result.values()) {
                if (!b) granted = false;
            }
            LcaiManager.Internal.getPermissionResult().onPermissionResult(granted);
            finish();
        });

        if (permissionArray != null && permissionArray.length > 0) {
            permissionLauncher.launch(permissionArray);
        }
    }
}
