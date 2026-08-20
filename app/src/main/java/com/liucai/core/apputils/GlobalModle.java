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

    public Object getModle(String key) {
        if (_modle != null && _modle.size() > 0) {
            return _modle.get(key);
        }
        LcaiLogUtils.w("get key=>"+key+"失败");
        return null;
    }

    public void remove(String key, Object value) {
        boolean isHave = true;
        if (_modle != null && _modle.size() > 0) {
            for (Map.Entry<String, Object> entry : _modle.entrySet()) {
                if (entry.getValue() == value) {
                    isHave = true;
                    break;
                }
            }
            if (isHave) {
                _modle.remove(key);
            }
        }
    }

    public void clearModle() {
        _modle = null;
    }
}
