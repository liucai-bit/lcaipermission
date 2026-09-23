package com.liucai.preference;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/9/23
 */
final class Editor {
    private final SharedPreferences.Editor editor;

    Editor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    Editor put(@NonNull String key, @Nullable Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            editor.putLong(key, Double.doubleToRawLongBits((Double) value));
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        }
        // 其它类型静默忽略，或按需抛异常
        return this;
    }

    Editor putLong(@NonNull String key, double value) {
        return put(key, value);
    }

    Editor remove(@NonNull String key) {
        editor.remove(key);
        return this;
    }

    Editor clear() {
        editor.clear();
        return this;
    }

    void apply() {
        editor.apply();
    }

    boolean commit() {
        return editor.commit();
    }
}
