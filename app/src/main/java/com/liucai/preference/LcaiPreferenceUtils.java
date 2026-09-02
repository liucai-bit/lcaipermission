package com.liucai.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.apputils.GlobalModleString;
import com.liucai.core.exception.LcaiHttpException;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/13
 */
public class LcaiPreferenceUtils {
    private static SharedPreferences preferences;
    private static final class PreferenceModle{
        public static LcaiPreferenceUtils modle = new LcaiPreferenceUtils();
    }

    public static LcaiPreferenceUtils getModle() {
        return PreferenceModle.modle;
    }

    public static boolean isInit() {
        return preferences != null;
    }

    public void init() {
        Context context = GlobalAppUtil.getApplicationContext();
        preferences = context.getSharedPreferences(GlobalModleString.GLOBAL_PREFERENCE, Context.MODE_PRIVATE);
    }

    /**
     * 存储缓存
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        verifyModle();
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Double) {
            editor.putFloat(key,Float.valueOf(value.toString()));
        }
        editor.commit();
    }

    /**
     * 获取换中数据
     * @param key
     * @param defaultObject
     * @return
     */
    public Object get(String key,Object defaultObject) {
        verifyModle();
        if (defaultObject instanceof String) {
            return preferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return preferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return preferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Float) {
            return preferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return preferences.getLong(key, (Long) defaultObject);
        } else if (defaultObject instanceof Double) {
            return preferences.getFloat(key,Float.valueOf(defaultObject.toString()));
        }
        return null;
    }

    /**
     * 查询是否存在某个key
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        verifyModle();
        return preferences.contains(key);
    }

    /**
     * 删除某个key值
     * @param key
     */
    public void removeKey(String key) {
        verifyModle();
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清除缓存
     */
    public void clear() {
        verifyModle();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
    }

    private void verifyModle() {
        if (preferences == null) {
            throw new LcaiHttpException("必须先初始化LcaiPreferenceUtils");
        }
    }
}
