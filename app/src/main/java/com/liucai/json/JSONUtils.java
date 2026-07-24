package com.liucai.json;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/23
 */
public class JSONUtils{

    /**
     * 将字符串转换成json
     * @param data
     * @return
     */
    public static JSONObject parse(String data) {
        JSONObject jsonObject = null;
        try {
            if (isJson(data)) {
                jsonObject = JSONObject.parseObject(data);
            } else {
                jsonObject = new JSONObject();
            }
        } catch (Exception e) {
            jsonObject = new JSONObject();
            LcaiLogUtils.d("对象转换失败",data);
        }
        return jsonObject;
    }

    /**
     * 将字符串转换成array
     * @param data
     * @return
     */
    public static JSONArray parsrArray(String data) {
        JSONArray jsonArray = null;
        try {
            if (isJson(data)) {
                jsonArray = JSON.parseArray(data);
            }else{
                jsonArray = new JSONArray();
            }
        } catch (Exception e) {
            jsonArray = new JSONArray();
            LcaiLogUtils.d("对象转换失败",data);
        }
        return jsonArray;
    }

    public static int getInt(JSONObject jsonObject,String key, int defaultValue) {
        try {
            if (jsonObject == null || TextUtils.isEmpty(key)) {
                return defaultValue;
            }
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            return jsonObject.getIntValue(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        try {
            if (jsonObject == null || TextUtils.isEmpty(key)) {
                return defaultValue;
            }
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            return jsonObject.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(JSONObject jsonObject, String key, Boolean defaultValue) {
        try {
            if (jsonObject == null || TextUtils.isEmpty(key)) {
                return defaultValue;
            }
            if (!jsonObject.containsKey(key)) {
                return defaultValue;
            }
            return jsonObject.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 判断字符串是否是json字符串
     * @param arg
     * @return
     */
    public static boolean isJson(String arg) {
        if (TextUtils.isEmpty(arg)) {
            LcaiLogUtils.w("字符串为空");
            return false;
        }
        try {
            JSON.parse(arg);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
