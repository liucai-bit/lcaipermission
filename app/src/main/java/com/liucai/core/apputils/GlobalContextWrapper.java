package com.liucai.core.apputils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;

import com.liucai.preference.LcaiPreferenceUtils;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/14
 */
public class GlobalContextWrapper extends ContextWrapper {

    private Context mContext;
    public GlobalContextWrapper(Context base) {
        super(base);
        this.mContext = base;
    }

    public ContextWrapper wrap() {
        float fontScale = (float) LcaiPreferenceUtils.getModle().get(GlobalModleString.GLOBAL_FONT_SCALE, 1.0);
        if (fontScale > 0) {
            Configuration config = mContext.getResources().getConfiguration();

            // 如果当前缩放比例与目标一致，则无需包装
            if (config.fontScale == fontScale) {
                return new GlobalContextWrapper(mContext);
            }

            Configuration newConfig = new Configuration(config);
            newConfig.fontScale = fontScale;

            // 根据 API 级别更新配置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mContext = mContext.createConfigurationContext(newConfig);
            } else {
                // 旧版本处理方式（已废弃，但兼容低版本）
                mContext.getResources().updateConfiguration(newConfig, mContext.getResources().getDisplayMetrics());
            }
            return new GlobalContextWrapper(mContext);
        }

        return new GlobalContextWrapper(mContext);

    }
}
