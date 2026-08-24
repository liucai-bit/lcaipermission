package com.liucai.component.bean;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/30
 */
public class BaseComponentConfig {
    public int marginLR;

    public int marginTB;

    public int paddingLR;

    public int paddingTB;

    public int titleColor= Color.parseColor("#1C1C1C");

    public int titleSize=16;

    public Drawable componentBackground;

    public BaseComponentConfig setMarginLR(int marginLR) {
        this.marginLR = marginLR;
        return this;
    }

    public BaseComponentConfig setMarginTB(int marginTB) {
        this.marginTB = marginTB;
        return this;
    }

    public BaseComponentConfig setPaddingLR(int paddingLR) {
        this.paddingLR = paddingLR;
        return this;
    }

    public BaseComponentConfig setPaddingTB(int paddingTB) {
        this.paddingTB = paddingTB;
        return this;
    }

    public BaseComponentConfig setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public BaseComponentConfig setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public BaseComponentConfig setComponentBackground(Drawable componentBackground) {
        this.componentBackground = componentBackground;
        return this;
    }



}
