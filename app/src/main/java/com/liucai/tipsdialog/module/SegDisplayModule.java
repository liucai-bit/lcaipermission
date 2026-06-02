package com.liucai.tipsdialog.module;

/**
 * @author HUAWEI
 * @program jgxt
 * @description
 * @Date 2026/5/12
 */
public class SegDisplayModule {

    /**
     * 显示字符串
     */
    public String text;

    /**
     * 是否可以点击
     */
    public boolean clickEnabel;

    /**
     * 点击Index
     */
    public String clickIndex;

    /**
     * 显示文字颜色
     */
    public String textColor="#1c1c1c";


    public SegDisplayModule setText(String text) {
        this.text = text;
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
        return this;
    }
}
