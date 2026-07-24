package com.liucai.jsbridge.web;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/7
 */
public class WebActivityConfig {

    private static final Object CALLBACK_LOCK = new Object();

    static final class WebActivityConfigHelper {
        public static WebActivityConfig config = new WebActivityConfig();

    }

    private WebActivityConfig() {

    }

    public static WebActivityConfig getConfig() {
        return WebActivityConfigHelper.config;
    }

    /**
     * 标题
     */
    public String title;
    /**
     * 加载路径
     */
    public String url;
    /**
     * 申请权限
     */
    public String[] permissionArray;
    /**
     * 注册方法名
     */
    public String[] methodArrays;

    public WebActivityCallback callback;

    public WebActivityConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public WebActivityConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public WebActivityConfig setPermissionArrays(String ... permissionArray) {
        this.permissionArray = permissionArray;
        return this;
    }

    public WebActivityConfig setMethodArrays(String ... methodArrays) {
        this.methodArrays = methodArrays;
        return this;
    }

    public WebActivityConfig setCallBack(WebActivityCallback callback) {
        synchronized (CALLBACK_LOCK) {
            this.callback = callback;
        }
        return this;
    }

    public WebActivityCallback getCallback() {
        synchronized (CALLBACK_LOCK) {
            WebActivityCallback callback1 = callback;
            return callback1;
        }
    }

    public void clear() {
        WebActivityConfigHelper.config = new WebActivityConfig();
    }

}
