package com.liucai.jsbridge.web;

import android.app.Activity;
import android.webkit.PermissionRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.LcaiManager;
import com.liucai.permission.bulider.LcaiPermissionRequestBulider;
import com.liucai.permission.core.LcaiReqPermissionResult;

import java.util.Map;

/**
 * WebView 权限桥接。
 * @author liucai
 */
public final class LcaiPermissionBridge {

    private LcaiPermissionBridge() {
    }

    public interface OnPermissionDenied {
        void onDenied(Map<String, Boolean> permissions);
    }

    public static void handle(@NonNull Activity activity,
                              @Nullable PermissionRequest request,
                              @Nullable String[] permissionArray,
                              @Nullable OnPermissionDenied deniedCallback) {
        if (request == null) return;

        if (permissionArray == null || permissionArray.length == 0) {
            safeGrant(request);
            return;
        }

        LcaiManager.getInstance().permissionReq(new LcaiPermissionRequestBulider()
                .with(activity)
                .addPermission(permissionArray)
                .check(true)
                .addResult(new LcaiReqPermissionResult() {
                    @Override
                    public void onReqPermissionPass() {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        safeGrant(request);
                    }

                    @Override
                    public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        safeDeny(request);
                        if (deniedCallback != null) deniedCallback.onDenied(permissions);
                    }
                }));
    }

    private static void safeGrant(PermissionRequest request) {
        try {
            request.grant(request.getResources());
        } catch (Exception ignored) {
        }
    }

    private static void safeDeny(PermissionRequest request) {
        try {
            request.deny();
        } catch (Exception ignored) {
        }
    }
}