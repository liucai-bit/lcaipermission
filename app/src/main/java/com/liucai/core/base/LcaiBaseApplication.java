package com.liucai.core.base;

import android.app.Application;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.exception.LcaiUncaughtExceptionHandler;

/**
 * @author liucai
 */
public abstract class LcaiBaseApplication extends Application {

    public abstract void init();

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalAppUtil.init(this);
        if (GlobalAppUtil.isDebug()) {
            Thread.setDefaultUncaughtExceptionHandler(
                    LcaiUncaughtExceptionHandler.getInstance());
        }
        init();
    }
}