package com.liucai.core.apputils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.system.SystemUtils;
import com.liucai.preference.LcaiPreferenceUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * 全局应用上下文与运行时信息工具。
 *
 * <p>使用前必须先在 {@link Application#onCreate()} 中调用 {@link #init(Application)}。
 *
 * @author liucai
 */
public final class GlobalAppUtil {

    private static volatile Application sApplication;
    private static volatile String sVersionName;
    private static volatile int sVersionCode = -1;
    private static volatile boolean sSaveLog;

    private static volatile WeakReference<Activity> sCurrentActivityRef;

    private GlobalAppUtil() {
        // no instance
    }

    // ---------------- 初始化 ----------------

    public static void init(@NonNull Application application) {
        sApplication = application;
        LcaiPreferenceUtils.init();
        registerActivityLifecycleCallback(application);
    }

    private static void registerActivityLifecycleCallback(@NonNull Application app) {
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                sCurrentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                clearCurrentActivityIfMatch(activity);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                clearCurrentActivityIfMatch(activity);
            }
        });
    }

    private static void clearCurrentActivityIfMatch(@NonNull Activity activity) {
        WeakReference<Activity> ref = sCurrentActivityRef;
        if (ref != null && ref.get() == activity) {
            sCurrentActivityRef = null;
        }
    }

    // ---------------- Application / Context / Activity ----------------

    @NonNull
    public static Application getApplication() {
        Application app = sApplication;
        if (app == null) {
            throw new IllegalStateException("GlobalAppUtil has not been initialized, call init() first.");
        }
        return app;
    }

    @NonNull
    public static Context getApplicationContext() {
        return getApplication().getApplicationContext();
    }

    @Nullable
    public static Activity getActivity() {
        WeakReference<Activity> ref = sCurrentActivityRef;
        return ref == null ? null : ref.get();
    }

    /**
     * Activity 是否仍然存活（未 finishing 且未 destroyed）。
     */
    public static boolean isRunning(@Nullable Activity activity) {
        return activity != null
                && !activity.isFinishing()
                && !activity.isDestroyed();
    }

    // ---------------- 缓存目录 / 日志开关 ----------------

    @Nullable
    public static File getCacheFile() {
        return getApplicationContext().getExternalCacheDir();
    }

    public static void setSaveLog(boolean save) {
        sSaveLog = save;
    }

    public static boolean isSaveLog() {
        return sSaveLog;
    }

    // ---------------- 全局运行时存储 ----------------

    private static final GlobalModel GLOBAL = new GlobalModel();

    public static void globalSetObject(@NonNull String key, @Nullable Object value) {
        GLOBAL.set(key, value);
    }

    @Nullable
    public static <T> T globalGetObject(@NonNull String key, @Nullable T defaultValue) {
        return GLOBAL.get(key, defaultValue);
    }

    public static void globalRemoveObject(@NonNull String key) {
        GLOBAL.remove(key);
    }

    public static void globalClearObject() {
        GLOBAL.clear();
    }

    // ---------------- 屏幕尺寸相关 ----------------

    public static int getStatusBarHeight() {
        return getSystemDimen("status_bar_height");
    }

    public static int getNavigationBarHeight() {
        return getSystemDimen("navigation_bar_height");
    }

    private static int getSystemDimen(@NonNull String name) {
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier(name, "dimen", "android");
        if (resourceId != 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        LcaiLogUtils.w("获取 " + name + " 失败");
        return 0;
    }

    // ---------------- 字体缩放 ----------------

    public static float getSystemFontScale() {
        return LcaiPreferenceUtils.getFloat(GlobalModelString.GLOBAL_FONT_SCALE, 1.0f);
    }

    public static void setSystemFontScale(float fontScale) {
        float old = LcaiPreferenceUtils.getFloat(GlobalModelString.GLOBAL_FONT_SCALE, 1.0f);
        if (Float.compare(old, fontScale) == 0) {
            return;
        }
        LcaiPreferenceUtils.put(GlobalModelString.GLOBAL_FONT_SCALE, fontScale);
        Activity activity = getActivity();
        if (isRunning(activity)) {
            // 设置后需要重建 Activity 让配置生效
            activity.recreate();
        }
    }

    // ---------------- 应用信息 ----------------

    @NonNull
    public static String getDataDir() {
        return getApplicationContext().getApplicationInfo().dataDir;
    }

    @NonNull
    public static String getAppVersionName() {
        if (sVersionName != null) {
            return sVersionName;
        }
        String name = "1.0";
        try {
            Context ctx = getApplicationContext();
            PackageInfo info = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            if (info.versionName != null) {
                name = info.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LcaiLogUtils.i("获取版本名称信息失败");
        }
        sVersionName = name;
        return name;
    }

    public static int getAppVersionCode() {
        if (sVersionCode != -1) {
            return sVersionCode;
        }
        int code = 1;
        try {
            Context ctx = getApplicationContext();
            PackageInfo info = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LcaiLogUtils.i("获取版本号信息失败");
        }
        sVersionCode = code;
        return code;
    }

    // ---------------- 设备信息 ----------------

    public static String getSystemModel() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getDeviceProduct() {
        return Build.PRODUCT;
    }

    public static String getDeviceBoard() {
        return Build.BOARD;
    }

    public static String getDeviceDevice() {
        return Build.DEVICE;
    }

    public static String getDeviceFingerprint() {
        return Build.FINGERPRINT;
    }

    public static String getDeviceHardware() {
        return Build.HARDWARE;
    }

    public static String getDeviceHost() {
        return Build.HOST;
    }

    public static String getDeviceDisplay() {
        return Build.DISPLAY;
    }

    public static String getDeviceId() {
        return Build.ID;
    }

    public static String getDeviceUser() {
        return Build.USER;
    }

    /**
     * @deprecated Android 10+ 已不可用，始终返回 "unknown"。
     */
    @Deprecated
    public static String getDeviceSerial() {
        return Build.SERIAL;
    }

    public static int getDeviceSDK() {
        return Build.VERSION.SDK_INT;
    }

    public static String getDeviceAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    // ---------------- 网络 / 模拟器 ----------------

    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    public static boolean isEmulator() {
        return SystemUtils.CheckEmulatorFiles()
                || SystemUtils.CheckEmulatorBuild();
    }
}