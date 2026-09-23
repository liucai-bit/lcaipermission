package com.liucai.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.apputils.GlobalModelString;

import java.util.Set;

/**
 * SharedPreferences 工具类。
 *
 * <p>使用前必须先调用 {@link #init()}，否则所有读写方法会抛出
 * {@link IllegalStateException}。
 *
 * <p>写操作统一使用 {@link SharedPreferences.Editor#apply()}（异步提交，避免主线程 ANR），
 * 如需同步落盘请调用 {@link #commit()}。
 */
public final class LcaiPreferenceUtils {

    private static volatile SharedPreferences sPreferences;

    private LcaiPreferenceUtils() {
        // no instance
    }

    /**
     * 初始化，幂等且线程安全。
     */
    public static void init() {
        if (sPreferences == null) {
            synchronized (LcaiPreferenceUtils.class) {
                if (sPreferences == null) {
                    Context context = GlobalAppUtil.getApplicationContext();
                    sPreferences = context.getSharedPreferences(
                            GlobalModelString.GLOBAL_PREFERENCE, Context.MODE_PRIVATE);
                }
            }
        }
    }

    public static boolean isInit() {
        return sPreferences != null;
    }

    @NonNull
    private static SharedPreferences prefs() {
        SharedPreferences p = sPreferences;
        if (p == null) {
            throw new IllegalStateException(
                    "LcaiPreferenceUtils has not been initialized, call init() first.");
        }
        return p;
    }
    /**
     * 异步写入（推荐）。支持 String / Boolean / Integer / Long / Float / Double / Set&lt;String&gt;。
     */
    public static void put(@NonNull String key, @Nullable Object value) {
        editor().put(key, value).apply();
    }

    /**
     * 同步写入，写完后立即落盘。仅在确有必要时使用（如进程被杀前保存关键数据）。
     */
    public static boolean commit() {
        return editor().commit();
    }

    // ---------------- 读取 ----------------

    public static String getString(@NonNull String key, @Nullable String defValue) {
        return prefs().getString(key, defValue);
    }

    public static boolean getBoolean(@NonNull String key, boolean defValue) {
        return prefs().getBoolean(key, defValue);
    }

    public static int getInt(@NonNull String key, int defValue) {
        return prefs().getInt(key, defValue);
    }

    public static long getLong(@NonNull String key, long defValue) {
        return prefs().getLong(key, defValue);
    }

    public static float getFloat(@NonNull String key, float defValue) {
        return prefs().getFloat(key, defValue);
    }

    /**
     * Double 在 SharedPreferences 中没有原生支持，
     * 以 long bits 的方式存储以避免精度丢失。
     */
    public static void putDouble(@NonNull String key, double value) {
        editor().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    public static double getDouble(@NonNull String key, double defValue) {
        long bits = prefs().getLong(key, Double.doubleToRawLongBits(defValue));
        return Double.longBitsToDouble(bits);
    }

    @Nullable
    public static Set<String> getStringSet(@NonNull String key, @Nullable Set<String> defValue) {
        return prefs().getStringSet(key, defValue);
    }

    // ---------------- 其它 ----------------

    public static boolean hasKey(@NonNull String key) {
        return prefs().contains(key);
    }

    public static void removeKey(@NonNull String key) {
        editor().remove(key).apply();
    }

    /**
     * 清除全部缓存（原实现漏掉了 commit/apply，这里修复）。
     */
    public static void clear() {
        editor().clear().apply();
    }

    // ---------------- 内部工具 ----------------

    /**
     * 统一写入口，负责分派类型。
     */
    private static Editor editor() {
        return new Editor(prefs().edit());
    }
}