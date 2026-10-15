package com.liucai.permission.bulider;

import android.app.Activity;

import com.liucai.core.LcaiManager;
import com.liucai.core.apputils.GlobalModelString;
import com.liucai.core.util.text.TextUtils;
import com.liucai.json.JSONUtils;
import com.liucai.permission.bean.LcaiPermissionEntity;
import com.liucai.permission.core.LcaiPermissionResult;
import com.liucai.permission.core.LcaiReqPermissionResult;
import com.liucai.permission.core.PermissionsState;
import com.liucai.preference.LcaiPreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/10/14
 */
public class LcaiPermissionUtils {
    public static LcaiPermissionUtils instance;
    private Activity mActivity;
    private LcaiPermissionResult result;
    private List<String> permissionList;
    /**总的权限*/
    private int total;
    /**通过的权限*/
    private int granted;
    /**未通过的权限*/
    private int denied;
    /**永久拒绝的权限*/
    private int permanentlyDenied;

    public static LcaiPermissionUtils getInstance() {
        synchronized (LcaiPermissionUtils.class){
            if (instance == null) {
                instance = new LcaiPermissionUtils();
            }
            return instance;
        }
    }

    public LcaiPermissionUtils with(Activity mActivity) {
        this.mActivity = mActivity;
        return this;
    }

    public LcaiPermissionUtils addResult(LcaiPermissionResult result) {
        this.result = result;
        return this;
    }

    public LcaiPermissionUtils addVerifyPermission(String permission) {
        if (permissionList == null) {
            permissionList = new ArrayList<>();
        }
        permissionList.add(permission);
        return this;
    }

    public LcaiPermissionUtils addVerifyPermission(String... permissions) {
        if (permissionList == null) {
            permissionList = new ArrayList<>();
        }
        Collections.addAll(permissionList, permissions);
        return this;
    }

    public void verify() {
        total = permissionList.size();
        denied = 0;
        permanentlyDenied = 0;
        LcaiManager.getInstance().permissionReq(
                new LcaiPermissionRequestBulider()
                        .with(mActivity)
                        .check(true)
                        .addPermission(permissionList.toArray(new String[permissionList.size()]))
                        .addResult(new LcaiReqPermissionResult() {
                            @Override
                            public void onReqPermissionPass() {
                                //权限通过
                                if (result != null) {
                                    result.onPermissionsState(PermissionsState.ALL_GRANTED);
                                }
                            }

                            @Override
                            public void onReqPermissionNoPass(Map<String, Boolean> permissions) {
                                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                                    if (entry.getValue()) {
                                        permanentlyDenied++;
                                    }else {
                                        denied++;
                                    }
                                }
                                if (result != null) {
                                    if (denied == total) {
                                        //全部没权限
                                        result.onPermissionsState(PermissionsState.NEVER_REQUESTED);
                                        return;
                                    }

                                    if (permanentlyDenied == total) {
                                        //全部永久拒绝
                                        result.onPermissionsState(PermissionsState.ALL_DENIED_PERMANENT);
                                        return;
                                    }

                                    if (denied > 0 && permanentlyDenied > 0) {
                                        //有的授权，有的拒绝，或有的永久拒绝
                                        result.onPermissionsState(PermissionsState.MIXED);
                                        return;
                                    }

                                    if (denied > 0) {
                                        //有些通过，有些没通过
                                        result.onPermissionsState(PermissionsState.PARTIALLY_REQUESTED);
                                        return;
                                    }

                                    if (permanentlyDenied > 0) {
                                        //有些通过，有些永久拒绝
                                        result.onPermissionsState(PermissionsState.PARTIALLY_REQUESTED);
                                        return;
                                    }
                                }
                            }
                        }));
    }

    public void isRequest() {
        List<LcaiPermissionEntity> entities = new ArrayList<>();
        List<LcaiPermissionEntity> all = loadAllRecords();
        if (all.isEmpty()) {
            if (result != null) {
                result.onPermissionRequest(false, entities);
            }
            return;
        }

        if (permissionList == null || permissionList.size() == 0) {
            for (LcaiPermissionEntity r : all) {
                entities.add(r);
            }
        } else {
            for (String p : permissionList) {
                LcaiPermissionEntity r = getRecord(p);
                if (r != null) {
                    entities.add(r);
                }
            }
        }

        boolean requested = !entities.isEmpty();
        if (result != null) {
            result.onPermissionRequest(requested, entities);
        }

    }

    private List<LcaiPermissionEntity> loadAllRecords() {
        String raw = LcaiPreferenceUtils.getString(GlobalModelString.PERMISSION_APPLIED, "");
        if (TextUtils.isEmpty(raw)) return new ArrayList<>();
        List<LcaiPermissionEntity> list = JSONUtils.parsrArray(raw, LcaiPermissionEntity.class);
        return list != null ? list : new ArrayList<>();
    }

    private LcaiPermissionEntity getRecord(String permission) {
        if (TextUtils.isEmpty(permission)) return null;
        for (LcaiPermissionEntity r : loadAllRecords()) {
            if (permission.equals(r.permission)) {
                return r;
            }
        }
        return null;
    }
}
