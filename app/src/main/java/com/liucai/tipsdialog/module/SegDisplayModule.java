package com.liucai.tipsdialog.module;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/12
 */
@SuppressWarnings("unused")
public class SegDisplayModule {

    /**显示字符串*/
    public String text;
    public int textRes;

    /**是否可以点击*/
    public boolean clickEnabel;

    /**点击Index*/
    public String clickIndex;

    /**显示文字颜色*/
    public String textColor="#1c1c1c";
    public int textColorRes;


    public SegDisplayModule setText(String text) {
        this.text = text;
        this.textRes = 0;
        return this;
    }

    public SegDisplayModule setTextRes(@StringRes int resId) {
        this.textRes = resId;
        this.text = "";
        return this;
    }

    public SegDisplayModule isClickEnabel() {
        this.clickEnabel = true;
        return this;
    }

    public SegDisplayModule setClickIndex(String clickIndex) {
        this.clickIndex = clickIndex;
        return this;
    }

    public SegDisplayModule setTextColor(String textColor) {
        this.textColor = textColor;
        this.textColorRes = 0;
        return this;
    }

    public SegDisplayModule setTextColorRes(@ColorRes int resId) {
        this.textColorRes = resId;
        this.textColor = "";
        return this;
    }

    public String resolveString(@NonNull Context ctx) {
        if (!TextUtils.isEmpty(text)) return text;
        if (textRes!=0) return GlobalAppUtil.resolveString(ctx, textRes);
        return "";
    }

    public int resolveColor(@NonNull Context ctx){
        if (textColorRes != 0) {
            return GlobalAppUtil.resolveColor(ctx, textColorRes);
        }
        if (textColor != null) {
            try {
                return Color.parseColor(textColor);
            } catch (Throwable t) {
                LcaiLogUtils.e("SegDisplayModule invalid textColor=" + textColor, t);
                return Color.parseColor("#1c1c1c");
            }
        }
        return Color.parseColor("#1c1c1c");
    }
}
