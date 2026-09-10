package com.liucai.core.base;

import android.app.Application;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.exception.LcaiException;
import com.liucai.permission.BuildConfig;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/1
 */
public abstract class LcaiBaseApplication extends Application {
    public abstract void init();
    @Override
    public void onCreate() {
        super.onCreate();
        GlobalAppUtil.init(this);
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new LcaiException());
        }
        init();
    }
}
