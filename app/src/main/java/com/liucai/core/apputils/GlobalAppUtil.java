package com.liucai.core.apputils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.base.LcaiActivityManager;
import com.liucai.core.util.common.ComponentInterface;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.system.SystemUtils;
import com.liucai.preference.LcaiPreferenceUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局应用上下文与运行时信息工具（门面类）。
 * <p>使用前必须先在 {@link Application#onCreate()} 中调用 {@link #init(Application)}。
 *
 * @author liucai
 */
public final class GlobalAppUtil {

    private static volatile Application sApplication;
    private static volatile String sVersionName;
    private static volatile int sVersionCode = -1;
    private static volatile long sVersionCodeLong = -1L;
    private static volatile boolean sSaveLog;
    private static volatile boolean sDebug;
    private static volatile File sCacheDir;
    private static volatile File sCacheDir2;

    /**
     * 全局运行时存储，线程安全。
     */
    private static final ConcurrentHashMap<String, Object> GLOBAL = new ConcurrentHashMap<>();

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static ComponentInterface<?> anInterface;

    private GlobalAppUtil() {
        // no instance
    }

    // ==================== 初始化 ====================

    public static void init(@NonNull Application application) {
        if (sApplication == application) {
            return;
        }
        sApplication = application;
        sCacheDir = safeExternalCacheDir(application);
        sDebug = (application.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        LcaiPreferenceUtils.init();
        LcaiActivityManager.getInstance().init(application);
    }

    /**
     * 保留的历史 API，当前未在内部使用；如业务方有引用可继续调用。
     */
    public static void onComponentInterface(ComponentInterface<?> anInterface) {
        GlobalAppUtil.anInterface = anInterface;
    }

    @Nullable
    private static File safeExternalCacheDir(@NonNull Context context) {
        try {
            return context.getExternalCacheDir();
        } catch (Throwable t) {
            return null;
        }
    }

    // ==================== Application / Context / Activity ====================

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

    /**
     * 当前处于 resumed（前台可见）状态的 Activity，可能为 null。
     */
    @Nullable
    public static Activity getActivity() {
        return LcaiActivityManager.getInstance().getResumedActivity();
    }

    /**
     * Activity 是否仍然存活（未 finishing 且未 destroyed）。
     */
    public static boolean isRunning(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        }
        return true;
    }

    /**
     * 应用是否处于前台。
     */
    public static boolean isForeground() {
        return LcaiActivityManager.getInstance().isAppForeground();
    }

    /**
     * 应用是否处于后台。
     */
    public static boolean isBackground() {
        return !isForeground();
    }

    // ==================== 线程 ====================

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static Handler getMain() {
        return MAIN;
    }

    public static void runOnUiThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            MAIN.post(runnable);
        }
    }

    public static void runOnUiThreadDelayed(@NonNull Runnable runnable, long delayMillis) {
        MAIN.postDelayed(runnable, delayMillis);
    }

    public static void removeUiRunnable(@NonNull Runnable runnable) {
        MAIN.removeCallbacks(runnable);
    }

    // ==================== 缓存目录 / 日志开关 ====================

    @Nullable
    public static File getCacheFile() {
        return sCacheDir;
    }

    @Nullable
    public static File getCacheFile2() {
        File file = sCacheDir2;
        if (file == null) {
            file = safeExternalCacheDir(getApplicationContext());
            sCacheDir2 = file;
        }
        return file;
    }

    @NonNull
    public static File getInternalCacheDir() {
        return getApplicationContext().getCacheDir();
    }

    @NonNull
    public static File getFilesDir() {
        return getApplicationContext().getFilesDir();
    }

    public static void setSaveLog(boolean save) {
        sSaveLog = save;
    }

    public static boolean isSaveLog() {
        return sSaveLog;
    }

    public static void setIsDebug(boolean debug) {
        sDebug = debug;
    }

    public static boolean isDebug() {
        return sDebug;
    }

    // ==================== 全局运行时存储 ====================

    public static void globalSetObject(@NonNull String key, @Nullable Object value) {
        if (value == null) {
            GLOBAL.remove(key);
        } else {
            GLOBAL.put(key, value);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T globalGetObject(@NonNull String key, @Nullable T defaultValue) {
        Object value = GLOBAL.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    public static boolean globalContainsKey(@NonNull String key) {
        return GLOBAL.containsKey(key);
    }

    public static void globalRemoveObject(@NonNull String key) {
        GLOBAL.remove(key);
    }

    public static void globalClearObject() {
        GLOBAL.clear();
    }

    // ==================== 尺寸换算 ====================

    public static float getDensity() {
        return getApplicationContext().getResources().getDisplayMetrics().density;
    }

    public static int dp2px(float dp) {
        return (int) (dp * getDensity() + 0.5f);
    }

    public static int px2dp(float px) {
        return (int) (px / getDensity() + 0.5f);
    }

    public static int sp2px(float sp) {
        return (int) (sp * getApplicationContext().getResources()
                .getDisplayMetrics().scaledDensity + 0.5f);
    }

    public static int getScreenWidth() {
        return getApplicationContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return getApplicationContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static boolean isLandscape() {
        return getApplicationContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 是否为平板/大屏（smallestScreenWidthDp >= 600dp）。
     */
    public static boolean isTablet() {
        return getApplicationContext().getResources().getConfiguration()
                .smallestScreenWidthDp >= 600;
    }

    public static boolean isNightMode() {
        return (getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    // ==================== 状态栏 / 导航栏高度 ====================

    /**
     * 同步获取状态栏高度（优先资源值，兜底 24dp）。
     * <p>刘海屏下资源值可能不含刘海部分，如需精确值请用
     * {@link #getStatusBarHeight(Activity, ComponentInterface)}。
     */
    public static int getStatusBarHeight() {
        int height = getSystemDimenByRes("status_bar_height");
        return height > 0 ? height : dp2px(24);
    }

    /**
     * 异步获取精确状态栏高度（依据当前 Activity 的 WindowInsets）。
     */
    public static void getStatusBarHeight(@Nullable Activity activity,
                                          @Nullable ComponentInterface<Integer> callback) {
        if (!isRunning(activity)) {
            safeCallback(callback, getStatusBarHeight());
            return;
        }
        getStatusBarHeightReal(activity, callback);
    }

    /**
     * 同步获取导航栏高度（优先资源值，兜底 48dp）。
     */
    public static int getNavigationBarHeight() {
        String resName = isLandscape()
                ? "navigation_bar_height_landscape"
                : "navigation_bar_height";
        int height = getSystemDimenByRes(resName);
        if (height <= 0) {
            height = getSystemDimenByRes("navigation_bar_height");
        }
        return height > 0 ? height : dp2px(48);
    }

    /**
     * 异步获取精确导航栏高度（依据当前 Activity 的 WindowInsets）。
     */
    public static void getNavigationBarHeight(@Nullable Activity activity,
                                              @Nullable ComponentInterface<Integer> callback) {
        if (!isRunning(activity)) {
            safeCallback(callback, getNavigationBarHeight());
            return;
        }
        getRealNavigationBarHeight(activity, callback);
    }

    private static void safeCallback(@Nullable ComponentInterface<Integer> callback, int value) {
        if (callback != null) {
            callback.onComponentCallback(value);
        }
    }

    @Nullable
    private static WindowInsets getRootWindowInsetsCompat(@NonNull View decor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return decor.getRootWindowInsets();
            } catch (Throwable t) {
                LcaiLogUtils.i("读取 WindowInsets 失败: " + t.getMessage());
            }
        }
        return null;
    }

    private static void getStatusBarHeightReal(@NonNull Activity activity,
                                               @Nullable ComponentInterface<Integer> callback) {
        final View decor = activity.getWindow().getDecorView();
        WindowInsets insets = getRootWindowInsetsCompat(decor);
        if (tryReadStatusBarFromInsets(insets, callback)) {
            return;
        }
        decor.post(() -> {
            if (!isRunning(activity)) {
                safeCallback(callback, getStatusBarHeight());
                return;
            }
            WindowInsets postInsets = getRootWindowInsetsCompat(decor);
            if (tryReadStatusBarFromInsets(postInsets, callback)) {
                return;
            }
            Rect rect = new Rect();
            decor.getWindowVisibleDisplayFrame(rect);
            safeCallback(callback, rect.top > 0 ? rect.top : getStatusBarHeight());
        });
    }

    private static boolean tryReadStatusBarFromInsets(@Nullable WindowInsets insets,
                                                      @Nullable ComponentInterface<Integer> callback) {
        if (insets == null) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                safeCallback(callback, insets.getInsets(WindowInsets.Type.statusBars()).top);
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                safeCallback(callback, insets.getSystemWindowInsetTop());
                return true;
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("读取状态栏高度失败: " + t.getMessage());
        }
        return false;
    }

    private static void getRealNavigationBarHeight(@NonNull Activity activity,
                                                   @Nullable ComponentInterface<Integer> callback) {
        final View decor = activity.getWindow().getDecorView();
        WindowInsets insets = getRootWindowInsetsCompat(decor);
        if (tryReadNavBarFromInsets(insets, callback)) {
            return;
        }
        decor.post(() -> {
            if (!isRunning(activity)) {
                safeCallback(callback, getNavigationBarHeight());
                return;
            }
            WindowInsets postInsets = getRootWindowInsetsCompat(decor);
            if (tryReadNavBarFromInsets(postInsets, callback)) {
                return;
            }
            safeCallback(callback, getNavigationBarHeightByFrame(activity));
        });
    }

    private static boolean tryReadNavBarFromInsets(@Nullable WindowInsets insets,
                                                   @Nullable ComponentInterface<Integer> callback) {
        if (insets == null) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                safeCallback(callback, insets.getInsets(WindowInsets.Type.navigationBars()).bottom);
                return true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                safeCallback(callback, insets.getSystemWindowInsetBottom());
                return true;
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("读取导航栏高度失败: " + t.getMessage());
        }
        return false;
    }

    private static int getSystemDimenByRes(@NonNull String resName) {
        try {
            Resources res = getApplicationContext().getResources();
            int id = res.getIdentifier(resName, "dimen", "android");
            if (id > 0) {
                return res.getDimensionPixelSize(id);
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("读取系统 dimen 失败: " + resName + ", " + t.getMessage());
        }
        return 0;
    }

    private static int getNavigationBarHeightByFrame(@NonNull Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        return Math.max(dm.heightPixels - rect.bottom, 0);
    }

    // ==================== 字体缩放 ====================

    public static float getSystemFontScale() {
        return LcaiPreferenceUtils.getFloat(GlobalModelString.GLOBAL_FONT_SCALE, 1.0f);
    }

    /**
     * 设置应用内字体缩放。会重建当前 resumed 的 Activity；栈中其它 Activity
     * 的更新由 {@code attachBaseContext} 中的统一配置覆盖逻辑保证。
     */
    public static void setSystemFontScale(float fontScale) {
        float old = LcaiPreferenceUtils.getFloat(GlobalModelString.GLOBAL_FONT_SCALE, 1.0f);
        if (Float.compare(old, fontScale) == 0) {
            return;
        }
        LcaiPreferenceUtils.put(GlobalModelString.GLOBAL_FONT_SCALE, fontScale);
        Activity activity = getActivity();
        if (isRunning(activity)) {
            activity.recreate();
        }
    }

    // ==================== UI 工具 ====================

    public static void toast(@Nullable CharSequence message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        CharSequence msg = message;
        runOnUiThread(() -> {
            try {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            } catch (Throwable t) {
                LcaiLogUtils.i("Toast 失败: " + t.getMessage());
            }
        });
    }

    public static void copyText(@Nullable CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        try {
            ClipboardManager cm = (ClipboardManager)
                    getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("text", text));
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("复制文本失败: " + t.getMessage());
        }
    }

    public static void hideKeyboard(@Nullable Activity activity) {
        if (!isRunning(activity)) {
            return;
        }
        try {
            View focus = activity.getCurrentFocus();
            if (focus == null) {
                focus = activity.getWindow().getDecorView();
            }
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("隐藏键盘失败: " + t.getMessage());
        }
    }

    public static void showKeyboard(@Nullable View view) {
        if (view == null) {
            return;
        }
        try {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        } catch (Throwable t) {
            LcaiLogUtils.i("显示键盘失败: " + t.getMessage());
        }
    }

    /**
     * 屏幕常亮开关。
     */
    public static void keepScreenOn(@Nullable Activity activity, boolean on) {
        if (!isRunning(activity)) {
            return;
        }
        if (on) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * 防截屏开关（FLAG_SECURE）。
     */
    public static void setSecure(@Nullable Activity activity, boolean secure) {
        if (!isRunning(activity)) {
            return;
        }
        if (secure) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    // ==================== 应用信息 ====================

    @NonNull
    public static String getPackageName() {
        return getApplicationContext().getPackageName();
    }

    @NonNull
    public static String getAppName() {
        Context ctx = getApplicationContext();
        try {
            CharSequence name = ctx.getApplicationInfo()
                    .loadLabel(ctx.getPackageManager());
            return name == null ? "" : name.toString();
        } catch (Throwable t) {
            return "";
        }
    }

    @NonNull
    public static String getDataDir() {
        return getApplicationContext().getApplicationInfo().dataDir;
    }

    @NonNull
    public static String getAppVersionName() {
        String name = sVersionName;
        if (name != null) {
            return name;
        }
        name = "1.0";
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
        sVersionCode = (int) getAppVersionCodeLong();
        return sVersionCode;
    }

    /**
     * 版本号（long 版，避免高版本号溢出；API 28+ 推荐使用）。
     */
    public static long getAppVersionCodeLong() {
        long code = sVersionCodeLong;
        if (code != -1L) {
            return code;
        }
        code = 1L;
        try {
            Context ctx = getApplicationContext();
            PackageInfo info = ctx.getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                code = info.getLongVersionCode();
            } else {
                code = info.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LcaiLogUtils.i("获取版本号信息失败");
        }
        sVersionCodeLong = code;
        return code;
    }

    /**
     * 是否安装指定包名的应用。
     */
    public static boolean isAppInstalled(@Nullable String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 是否首次启动（首次返回 true，并写入偏好）。
     */
    public static boolean isFirstLaunch() {
        String key = "app_first_launch";
        boolean first = LcaiPreferenceUtils.getBoolean(key, true);
        if (first) {
            LcaiPreferenceUtils.put(key, false);
        }
        return first;
    }

    // ==================== 进程 ====================

    @NonNull
    public static String getProcessName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return Application.getProcessName();
            } catch (Throwable ignored) {
                // 继续走反射
            }
        }
        try {
            Method m = Class.forName("android.app.ActivityThread")
                    .getDeclaredMethod("currentProcessName");
            m.setAccessible(true);
            Object result = m.invoke(null);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return getPackageName();
    }

    /**
     * 是否运行在主进程（多进程场景使用）。
     */
    public static boolean isMainProcess() {
        return getPackageName().equals(getProcessName());
    }

    // ==================== 设备信息 ====================

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

    // ==================== 网络 ====================

    @Nullable
    private static ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 是否有可用网络（连接中或已连接）。
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = getConnectivityManager();
        if (cm == null) {
            return false;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) {
                    return false;
                }
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                return caps != null
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnectedOrConnecting();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 是否连接 Wi-Fi。
     */
    public static boolean isWifiConnected() {
        return hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * 是否连接移动数据。
     */
    public static boolean isMobileConnected() {
        return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    private static boolean hasTransport(int transport) {
        ConnectivityManager cm = getConnectivityManager();
        if (cm == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        try {
            Network network = cm.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasTransport(transport);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 注册网络变化监听。调用方需在合适时机调用
     * {@link #unregisterNetworkCallback(ConnectivityManager.NetworkCallback)}。
     */
    public static void registerNetworkCallback(
            @NonNull ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager cm = getConnectivityManager();
        if (cm == null) {
            return;
        }
        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            cm.registerNetworkCallback(request, callback);
        } catch (Throwable t) {
            LcaiLogUtils.i("注册网络监听失败: " + t.getMessage());
        }
    }

    public static void unregisterNetworkCallback(
            @NonNull ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager cm = getConnectivityManager();
        if (cm == null) {
            return;
        }
        try {
            cm.unregisterNetworkCallback(callback);
        } catch (Throwable ignored) {
            // callback 未注册时会抛异常，忽略
        }
    }

    // ==================== 模拟器 ====================

    public static boolean isEmulator() {
        return SystemUtils.CheckEmulatorFiles()
                || SystemUtils.CheckEmulatorBuild();
    }

    // ==================== 系统设置跳转 ====================

    /**
     * 跳转到本应用设置页。
     */
    public static void openAppSettings(@Nullable Activity activity) {
        if (!isRunning(activity)) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Throwable t) {
            LcaiLogUtils.i("跳转应用设置失败: " + t.getMessage());
        }
    }

    /**
     * 跳转到网络设置页。
     */
    public static void openNetworkSettings(@Nullable Activity activity) {
        if (!isRunning(activity)) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Throwable t) {
            LcaiLogUtils.i("跳转网络设置失败: " + t.getMessage());
        }
    }

    /**
     * 跳转到本应用通知设置页（API 26+ 才存在）。
     */
    public static void openNotificationSettings(@Nullable Activity activity) {
        if (!isRunning(activity) || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Throwable t) {
            LcaiLogUtils.i("跳转通知设置失败: " + t.getMessage());
        }
    }

    // ==================== 内存 / 退出 / 重启 ====================

    /**
     * 获取可用内存（字节）。
     */
    public static long getAvailableMemory() {
        ActivityManager am = (ActivityManager)
                getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return 0;
        }
        try {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(info);
            return info.availMem;
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * 退出应用：结束所有 Activity 并杀进程。
     */
    public static void exitApp() {
        try {
            LcaiActivityManager.getInstance().finishAllActivities();
        } catch (Throwable t) {
            LcaiLogUtils.i("结束全部 Activity 失败: " + t.getMessage());
        }
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * 重启应用。
     */
    public static void restartApp() {
        Context ctx = getApplicationContext();
        Intent intent = ctx.getPackageManager()
                .getLaunchIntentForPackage(ctx.getPackageName());
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
        runOnUiThreadDelayed(GlobalAppUtil::exitApp, 200);
    }
}