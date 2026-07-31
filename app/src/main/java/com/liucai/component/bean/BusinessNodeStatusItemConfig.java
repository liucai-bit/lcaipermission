package com.liucai.component.bean;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/30
 */
public class BusinessNodeStatusItemConfig extends BaseComponentConfig{
    public int iconWidth=25;

    public int iconHeight=25;

    public int itemSpace=10;

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
}
