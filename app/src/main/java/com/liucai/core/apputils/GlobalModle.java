package com.liucai.core.apputils;

import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/14
 */
class GlobalModle {
    private Map<String, Object> _modle = new ConcurrentHashMap<>();

    public void setModle(String key,Object value) {
        if (key == null) throw new LcaiHttpException("key must not be null");
        if (value == null) throw new LcaiHttpException("value must not be null");
        _modle.put(key, value);
    }

    public Object getModle(String key,Object defaultValue) {
        Object value = _modle.get(key);
        return value != null ? value : defaultValue;
    }

    public void remove(String key) {
        _modle.remove(key);
    }

    public void clearModle() {
        _modle = new ConcurrentHashMap<>();
    }
}
