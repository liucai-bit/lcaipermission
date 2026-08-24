package com.liucai.component.bean;

import android.graphics.Color;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/7/28
 */
public class HorizonalLabelBean {
    public String label;

    public int textColor= Color.parseColor("#1c1c1c");

    public int textSize=10;

    public int border;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }
}
