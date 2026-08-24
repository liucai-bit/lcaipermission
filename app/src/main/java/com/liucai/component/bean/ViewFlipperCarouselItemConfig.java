package com.liucai.component.bean;

import android.graphics.Color;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/30
 */
public class ViewFlipperCarouselItemConfig {

    /**
     * 文字大小
     */
    public int fontSize = 14;

    /**
     * 文字颜色
     */
    public int fontColor = Color.parseColor("#1C1C1C");

    public ViewFlipperCarouselItemConfig setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public ViewFlipperCarouselItemConfig setFontColor(int fontColor) {
        this.fontColor = fontColor;
        return this;
    }
}
