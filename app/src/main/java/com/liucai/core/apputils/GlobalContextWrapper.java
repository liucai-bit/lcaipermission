package com.liucai.core.apputils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;

import com.liucai.preference.LcaiPreferenceUtils;

/**
 * 字体缩放 Context 包装器。
 *
 * <p>注意：不要修改原始 Resources 的 Configuration，否则会污染全局 Application。
 *
 * @author liucai
 */
public class GlobalContextWrapper extends ContextWrapper {

    private final Context mBaseContext;

    public GlobalContextWrapper(Context base) {
        super(base);
        this.mBaseContext = base;
    }

    public Context wrap() {
        float fontScale = LcaiPreferenceUtils.getFloat(
                GlobalModelString.GLOBAL_FONT_SCALE, 1.0f);

        if (!(fontScale > 0)
                || Float.isNaN(fontScale)
                || Float.isInfinite(fontScale)) {
            return this;
        }

        Configuration config = mBaseContext.getResources().getConfiguration();
        if (Float.compare(config.fontScale, fontScale) == 0) {
            return this;
        }

        Configuration newConfig = new Configuration(config);
        newConfig.fontScale = fontScale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Context configContext = mBaseContext.createConfigurationContext(newConfig);
            return configContext != null ? new GlobalContextWrapper(configContext) : this;
        }

        // API 17 以下不支持 createConfigurationContext。
        // 不要调用 updateConfiguration 修改原始 Resources，避免污染全局。
        return this;
    }
}