package com.liucai.core.apputils;

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
        if (_modle == null) {
            _modle = new ConcurrentHashMap<>();
        }
        _modle.put(key, value);
    }

    public Object getModle(String key,Object defaultValue) {
        if (_modle != null && _modle.size() > 0) {
            if (_modle.containsKey(key)) {
                return _modle.get(key);
            }
        }
        return defaultValue;
    }

    public void remove(String key) {
        if (_modle != null && _modle.size() > 0) {
            _modle.remove(key);
        }
    }

    public void clearModle() {
        if (_modle != null && _modle.size() > 0) {
            _modle.clear();
            _modle = null;
        }
    }
}
