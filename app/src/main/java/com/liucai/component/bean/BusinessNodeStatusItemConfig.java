package com.liucai.component.bean;

import android.graphics.drawable.Drawable;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/30
 */
public class BusinessNodeStatusItemConfig extends BaseComponentConfig{
    public int iconWidth=25;

    public int iconHeight=25;

    public int itemSpace=10;

    public int badgeSize;
    public int badgeColor;
    public Drawable badgeBackground;
    public int maxNumber;
    public boolean isMax;
    public int ellipsize;

    public BusinessNodeStatusItemConfig setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
        return this;
    }

    public BusinessNodeStatusItemConfig setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
        return this;
    }

    public BusinessNodeStatusItemConfig setItemSpace(int itemSpace) {
        this.itemSpace = itemSpace;
        return this;
    }


    // 以下是为您补全的链式调用方法
    public BusinessNodeStatusItemConfig setBadgeSize(int badgeSize) {
        this.badgeSize = badgeSize;
        return this;
    }

    public BusinessNodeStatusItemConfig setBadgeColor(int badgeColor) {
        this.badgeColor = badgeColor;
        return this;
    }

    public BusinessNodeStatusItemConfig setBadgeBackground(Drawable badgeBackground) {
        this.badgeBackground = badgeBackground;
        return this;
    }

    public BusinessNodeStatusItemConfig setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
        return this;
    }

    public BusinessNodeStatusItemConfig setIsMax(boolean isMax) {
        this.isMax = isMax;
        return this;
    }

    public BusinessNodeStatusItemConfig setEllipsize(int ellipsize) {
        this.ellipsize = ellipsize;
        return this;
    }
}
