package com.liucai.component.bean;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/27
 */
public class BusinessNodesStatusBean {
    /**
     * 按钮名称
     */
    public String label;

    /**
     * 按钮路径
     */
    public String icon;

    /**
     * 角标信息
     * >0 显示 <=0不显示
     */
    public int subscript;

    /**
     * 原生数据
     * 事件返回返回原生数据
     */
    public Object data;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getSubscript() {
        return subscript;
    }

    public void setSubscript(int subscript) {
        this.subscript = subscript;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
