package com.liucai.tipsdialog.bulider;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.liucai.tipsdialog.core.LcaiTipsDialog;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;
import com.liucai.tipsdialog.module.SegDisplayModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HUAWEI
 * @program lctipsdialog
 * @description
 * @Date 2026/5/27
 */
public class LcaiTipsDialogBulider {

    /**
     * 上下文对象
     */
    public Context mContext;

    public LcaiTipsDialogBulider with(Context mContext) {
        this.mContext = mContext;
        return this;
    }

    /**
     * 标题
     */
    public String title;

    public LcaiTipsDialogBulider addTitle(String title) {
        this.title = title;
        return this;
    }


    public int titleColor;

    /**
     * 标题颜色
     * 默认 #1c1c1c
     */
    public LcaiTipsDialogBulider addTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public int titleSize;

    /**
     * 标题大小
     * @param titleSize
     * @return
     */
    public LcaiTipsDialogBulider addTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public String content;

    /**
     * 内容
     * @param content
     * @return
     */
    public LcaiTipsDialogBulider addContent(String content) {
        this.content = content;
        return this;
    }

    public boolean contentClickDismiss;

    /**
     * 点击内容是否关闭弹窗
     * @return
     */
    public LcaiTipsDialogBulider isContentClickDismiss() {
        this.contentClickDismiss = true;
        return this;
    }

    public int contentColor;

    /**
     * 内容颜色
     * 默认 #1C1C1C
     * @param contentColor
     * @return
     */
    public LcaiTipsDialogBulider addContentColor(int contentColor) {
        this.contentColor = contentColor;
        return this;
    }

    public int contentSize;

    /**
     * 内容大小
     * @param contentSize
     * @return
     */
    public LcaiTipsDialogBulider addContentSize(int contentSize) {
        this.contentSize = contentSize;
        return this;
    }

    public boolean segDisplay;

    public List<SegDisplayModule> moduleList;

    /**
     * 分段显示数据
     * @param moduleList
     * @return
     */
    public LcaiTipsDialogBulider addSegdisplays(List<SegDisplayModule> moduleList) {
        this.moduleList = moduleList;
        this.segDisplay = true;
        return this;
    }

    /**
     * 分段显示数据
     *
     * @param module
     * @return
     */
    public LcaiTipsDialogBulider addSegdisplay(SegDisplayModule module) {
        this.segDisplay = true;
        if (moduleList == null) {
            moduleList = new ArrayList<>();
        }
        moduleList.add(module);
        return this;
    }

    public String cancelText;

    /**
     * 左侧按钮文字
     * @param cancelText
     * @return
     */
    public LcaiTipsDialogBulider addCancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    public int cancelColor;

    /**
     * 左侧按钮颜色
     * @param cancelColor
     * @return
     */
    public LcaiTipsDialogBulider addCancelColor(int cancelColor) {
        this.cancelColor = cancelColor;
        return this;
    }

    public int cancelSize;

    /**
     * 左侧按钮文字大小
     * @param cancelSize
     * @return
     */
    public LcaiTipsDialogBulider addCancelSize(int cancelSize) {
        this.cancelSize = cancelSize;
        return this;
    }

    public Drawable cancelBackground;

    /**
     * 左侧按钮背景
     * @param drawable
     * @return
     */
    public LcaiTipsDialogBulider addCancelBackground(int drawable) {
        this.cancelBackground = mContext.getResources().getDrawable(drawable);
        return this;
    }

    public String confirmText;

    /**
     * 右侧按钮文字
     * @param confirmText
     * @return
     */
    public LcaiTipsDialogBulider addConfirmText(String confirmText) {
        this.confirmText = confirmText;
        return this;
    }


    public int confirmColor;

    /**
     * 右侧按钮文字颜色
     * @param confirmColor
     * @return
     */
    public LcaiTipsDialogBulider addConfirmColor(int confirmColor) {
        this.confirmColor = confirmColor;
        return this;
    }

    public int confirmSize;

    /**
     * 右侧按钮文字大小
     * @param contentSize
     * @return
     */
    public LcaiTipsDialogBulider addConfirmSize(int contentSize) {
        this.confirmSize = contentSize;
        return this;
    }

    public Drawable confirmBackground;

    /**
     * 右侧按钮背景
     * @param drawable
     * @return
     */
    public LcaiTipsDialogBulider addConfirmBackground(int drawable) {
        this.confirmBackground = mContext.getResources().getDrawable(drawable);
        return this;
    }

    public Drawable tipsBackground;

    /**
     * 弹窗背景
     * @param drawable
     * @return
     */
    public LcaiTipsDialogBulider addTipsBackground(int drawable) {
        this.tipsBackground = mContext.getResources().getDrawable(drawable);
        return this;
    }

    public OnTipsDialogInterface dialogInterface;

    /**
     * 点击回调
     * @param dialogInterface
     * @return
     */
    public LcaiTipsDialogBulider addDialogInterface(OnTipsDialogInterface dialogInterface) {
        this.dialogInterface = dialogInterface;
        return this;
    }

    public LcaiTipsDialog bulid() {
        return new LcaiTipsDialog(this);
    }
}
