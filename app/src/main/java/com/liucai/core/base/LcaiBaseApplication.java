package com.liucai.core.base;

import android.app.Application;

import com.liucai.core.exception.LcaiException;
import com.liucai.permission.BuildConfig;

/**
 * @author liucai
 * @program lctipsdialog
 * @description
 * @Date 2026/6/1
 */
public class LcaiBaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new LcaiException());
        }
    }
}
