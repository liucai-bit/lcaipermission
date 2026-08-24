package com.liucai.core.apputils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.preference.LcaiPreferenceUtils;

import java.io.File;
import java.util.Locale;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/14
 */
public class GloabalAppUtil {
    private static GlobalModle modle;
    private static File CACHE_DIR;
    private static boolean saveLog;

    public static void init(Application application) {
        modle = new GlobalModle();
        modle.setModle(GlobalModleString.GLOBAL_APPLICATION, application);
        LcaiPreferenceUtils.getModle().init();
        registerActivityLifecycelCallback();
        CACHE_DIR = application.getApplicationContext().getExternalCacheDir();
    }

    public static void setIsDebug(boolean isDebug) {
        verifyModle();
        modle.setModle(GlobalModleString.GLOBAL_DEBUG_MODE, isDebug);
    }

    private static void registerActivityLifecycelCallback() {
        getApplicatioon().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                modle.setModle(GlobalModleString.CLIENT_ACTIVITY, activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                modle.remove(GlobalModleString.CLIENT_ACTIVITY);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                modle.remove(GlobalModleString.CLIENT_ACTIVITY);
            }
        });
    }

    /**
     * 获取application
     * @return
     */
    public static Application getApplicatioon() {
        verifyModle();
        return (Application) modle.getModle(GlobalModleString.GLOBAL_APPLICATION,null);
    }

    /**
     * 获取applicationContext
     * @return
     */
    public static Context getApplicationContext() {
        verifyModle();
        Application application = (Application) modle.getModle(GlobalModleString.GLOBAL_APPLICATION,null);
        return application.getApplicationContext();
    }

    /**
     * 获取当前的activity
     * @return
     */
    public static Activity getActivity() {
        verifyModle();
        Activity activity = (Activity) modle.getModle(GlobalModleString.CLIENT_ACTIVITY,null);
        return activity;
    }

    /**
     * 获取缓存文件
     * @return
     */
    public static File getCacheFile() {
        return CACHE_DIR;
    }

    /**
     * 打开日志存储
     * @return
     */
    public static void openSaveLog(boolean saveLog) {
        GloabalAppUtil.saveLog = saveLog;
    }

    public static boolean openSaveLog() {
        return GloabalAppUtil.saveLog;
    }

    /**
     * 存储参数
     * @param key
     * @param value
     */
    public static void globalSetObject(String key, Object value) {
        verifyModle();
        modle.setModle(key, value);
    }

    /**
     * 获取存储的值
     * @param key
     * @return
     */
    public static Object globalGetobject(String key,Object defaultValue) {
        verifyModle();
        return modle.getModle(key,defaultValue);
    }

    /**
     * 获取状态栏高度
     * @return
     */
    public static int getStatusBarHeight() {
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获取底部导航栏高度
     * @return
     */
    public static int getNavigationBarHeight() {
        Context context = getApplicationContext();
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获取系统全局字体缩放倍数
     * @return
     */
    public static float getSystemFontScale() {
        Float fontScale = (Float) LcaiPreferenceUtils.getModle().get(GlobalModleString.GLOBAL_FONT_SCALE, 1.0f);
        return fontScale;
    }

    /**
     * 设置系统全局字体缩放倍数
     * @param fontScale
     */
    public static void setSystemFontScale(float fontScale) {
        verifyModle();
        if (!LcaiPreferenceUtils.isInit()) {
            LcaiPreferenceUtils.getModle().init();
        }
        float fScale = (float) LcaiPreferenceUtils.getModle().get(GlobalModleString.GLOBAL_FONT_SCALE, 1.0f);
        if (fScale != fontScale) {
            LcaiPreferenceUtils.getModle().put(GlobalModleString.GLOBAL_FONT_SCALE, fontScale);
        }
        //设置后需要重启
        getActivity().recreate();
    }

    /**
     * 获取应用私有目录存储地址
     */
    public static String getDataDir() {
        verifyModle();
        Context context = getApplicationContext();
        return context.getApplicationInfo().dataDir;
    }

    /**
     * 获取当前应用版本名称
     * 1.0
     * @return
     */
    public static String getAppVersionName() {
        verifyModle();
        Context context = getApplicationContext();
        String mVersionName = "1.0";
        try {
            PackageManager packageManager = context.getPackageManager();
            // 获取当前应用的包名
            String packageName = context.getPackageName();
            // 通过包名获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            // 获取版本名称 (例如：1.0)
            mVersionName = packageInfo.versionName;
            // 获取版本号 (例如：1)
            int versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            LcaiLogUtils.i("获取版本名称信息失败");
        }

        return mVersionName;
    }

    /**
     * 获取版本号
     * @return
     */
    public static int getAppversionCode() {
        verifyModle();
        int versionCode = 1;
        Context context = getApplicationContext();
        try {
            PackageManager packageManager = context.getPackageManager();
            // 获取当前应用的包名
            String packageName = context.getPackageName();
            // 通过包名获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            // 获取版本号 (例如：1)
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            LcaiLogUtils.i("获取版本号信息失败");
        }
        return versionCode;
    }

    /**
     * 获取手机型号
     * @return
     */
    public static String getSystemModle() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取厂商名
     **/
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取产品名
     **/
    public static String getDeviceProduct() {
        return Build.PRODUCT;
    }

    /**
     * 获取手机主板名
     */
    public static String getDeviceBoard() {
        return Build.BOARD;
    }

    /**
     * 设备名
     **/
    public static String getDeviceDevice() {
        return Build.DEVICE;
    }

    /**
     * fingerprit 信息
     **/
    public static String getDeviceFubgerprint() {
        return Build.FINGERPRINT;
    }

    /**
     * 硬件名
     **/
    public static String getDeviceHardware() {
        return Build.HARDWARE;
    }

    /**
     * 主机
     **/
    public static String getDeviceHost() {
        return Build.HOST;
    }

    /**
     * 显示ID
     **/
    public static String getDeviceDisplay() {
        return Build.DISPLAY;
    }

    /**
     * ID
     **/
    public static String getDeviceId() {
        return Build.ID;
    }

    /**
     * 获取手机用户名
     **/
    public static String getDeviceUser() {
        return Build.USER;
    }

    /**
     * 获取手机 硬件序列号
     **/
    public static String getDeviceSerial() {
        return Build.SERIAL;
    }

    /**
     * 获取手机Android 系统SDK
     *
     * @return
     */
    public static int getDeviceSDK() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机Android 版本
     *
     * @return
     */
    public static String getDeviceAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前手机系统语言。
     */
    public static String getDeviceDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private static void verifyModle(){
        if (modle == null) {
            throw new LcaiHttpException("必须先初始化GloabalAppUtil");
        }
    }
}
