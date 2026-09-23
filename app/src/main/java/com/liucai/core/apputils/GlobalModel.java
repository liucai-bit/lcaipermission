package com.liucai.core.apputils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局运行时缓存容器。
 *
 * <p>所有 key/value 都存于 {@link ConcurrentHashMap}，线程安全。
 *
 * @author liucai
 */
final class GlobalModel {

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();

    void set(@NonNull String key, @Nullable Object value) {
        if (value == null) {
            store.remove(key);
        } else {
            store.put(key, value);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    <T> T get(@NonNull String key, @Nullable T defaultValue) {
        Object value = store.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    void remove(@NonNull String key) {
        store.remove(key);
    }

    void clear() {
        store.clear();
    }
}