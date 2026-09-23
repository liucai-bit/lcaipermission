package com.liucai.tipsdialog.bulider;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.tipsdialog.core.LcaiTipsDialog;
import com.liucai.tipsdialog.core.LcaiTipsMode;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;
import com.liucai.tipsdialog.module.SegDisplayModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/5/27
 */
public class LcaiTipsDialogBuilder<T extends BaseRecycleAdapter>{
    /** 弹窗对象*/
    private LcaiTipsDialog dialog;
    /** 上下文对象 */
    public Context mContext;
    /**
     * 弹窗模式
     */
    public LcaiTipsMode mode = LcaiTipsMode.DEFALUT_MODE;
    /** 自定义弹窗layout*/
    public int customLayout;
    /** 弹窗背景颜色/Drawable */
    public Drawable popupBg;
    /** 弹窗标题 */
    public String title;
    /** 标题文字颜色 */
    public int titleColor= Color.parseColor("#1C1C1C");
    /** 标题文字大小 (sp) */
    public int titleSize=18;
    /**LIST模式适配器*/
    public T adapter;
    /**列表是否纵向模式*/
    public boolean vertical=true;
    /**列表横向，每行多少个数据*/
    public int listCloumns;
    /** 弹窗内容 */
    public String content;
    /** 内容文字颜色 */
    public int contentColor= Color.parseColor("#1C1C1C");
    /** 内容文字大小 (sp) */
    public int contentSize=16;
    /** 点击内容是否关闭 */
    public boolean contentClickDismiss;
    /** 是否分段显示 (用于富文本协议) */
    public boolean isSegDisplay;
    /** 分段显示内容列表 */
    public List<SegDisplayModule> moduleList;
    /** 内容居中方式 */
    public int contentGravity;
    /** 取消按钮文字 */
    public String cancelText;
    /** 取消按钮文字颜色 */
    @ColorInt public int cancelColor= Color.parseColor("#1C1C1C");
    /** 取消按钮文字大小 (sp) */
    public int cancelSize=16;
    /** 取消按钮背景 */
    public Drawable cancelBg;
    /** 确认按钮文字 */
    public String confirmText;
    /** 确认按钮文字颜色 */
    public int confirmColor= Color.parseColor("#1C1C1C");
    /** 确认按钮文字大小 (sp) */
    public int confirmSize=16;
    /** 确认按钮背景 */
    public Drawable confirmBg;
    /** 是否允许点击外部关闭 */
    public boolean outSideCancal;
    /** 点击回调接口 */
    public OnTipsDialogInterface dialogInterface;

    /**
     * 设置上下文
     */
    public LcaiTipsDialogBuilder setContext(Context context) {
        this.mContext = context;
        return this;
    }

    /**
     * 设置弹窗模式
     */
    public LcaiTipsDialogBuilder setMode(LcaiTipsMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * 设置自定义弹窗内容
     * @param customLayout
     * @return
     */
    public LcaiTipsDialogBuilder setCustomLayout(int customLayout) {
        this.customLayout = customLayout;
        return this;
    }

    /**
     * 设置弹窗背景
     */
    public LcaiTipsDialogBuilder setPopupBg(Drawable popupBg) {
        this.popupBg = popupBg;
        return this;
    }

    /**
     * 设置标题文字
     */
    public LcaiTipsDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置标题文字颜色 (@ColorInt)
     */
    public LcaiTipsDialogBuilder setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    /**
     * 设置标题文字大小 (sp)
     */
    public LcaiTipsDialogBuilder setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    /**
     * 设置列表适配器
     * @param adapter
     * @return
     */
    public LcaiTipsDialogBuilder<T> setAdapter(T adapter) {
        this.adapter = adapter;
        return this;
    }

    /**
     * 设置list是否纵向模式
     * @param vertical
     * @return
     */
    public LcaiTipsDialogBuilder setListVertical(boolean vertical) {
        this.vertical = vertical;
        return this;
    }

    /**
     * 设置列表横向显示多少个
     * @param cloumns
     * @return
     */
    public LcaiTipsDialogBuilder setListCloumns(int cloumns) {
        this.listCloumns = cloumns;
        return this;
    }

    /**
     * 设置内容文字
     */
    public LcaiTipsDialogBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 设置内容文字颜色 (@ColorInt)
     */
    public LcaiTipsDialogBuilder setContentColor(int contentColor) {
        this.contentColor = contentColor;
        return this;
    }

    /**
     * 设置内容文字大小 (sp)
     */
    public LcaiTipsDialogBuilder setContentSize(int contentSize) {
        this.contentSize = contentSize;
        return this;
    }

    /**
     * 设置点击内容是否关闭
     */
    public LcaiTipsDialogBuilder setContentClickDismiss(boolean contentClickDismiss) {
        this.contentClickDismiss = contentClickDismiss;
        return this;
    }

    /**
     * 设置是否分段显示 (用于富文本协议)
     */
    public LcaiTipsDialogBuilder setSegDisplay(boolean isSegDisplay) {
        this.isSegDisplay = isSegDisplay;
        return this;
    }

    /**
     * 设置分段显示内容列表
     */
    public LcaiTipsDialogBuilder setModuleList(List<SegDisplayModule> moduleList) {
        this.moduleList = moduleList;
        return this;
    }

    /**
     * 添加单个分段显示模块
     */
    public LcaiTipsDialogBuilder addModule(SegDisplayModule module) {
        if (this.moduleList == null) {
            this.moduleList = new ArrayList<>();
        }
        this.moduleList.add(module);
        return this;
    }

    /**
     * 设置内容居中方式
     */
    public LcaiTipsDialogBuilder setContentGravity(int contentGravity) {
        this.contentGravity = contentGravity;
        return this;
    }

    /**
     * 设置取消按钮文字
     */
    public LcaiTipsDialogBuilder setCancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    /**
     * 设置取消按钮文字颜色 (@ColorInt)
     */
    public LcaiTipsDialogBuilder setCancelColor(int cancelColor) {
        this.cancelColor = cancelColor;
        return this;
    }

    /**
     * 设置取消按钮文字大小 (sp)
     */
    public LcaiTipsDialogBuilder setCancelSize(int cancelSize) {
        this.cancelSize = cancelSize;
        return this;
    }

    /**
     * 设置取消按钮背景
     */
    public LcaiTipsDialogBuilder setCancelBg(Drawable cancelBg) {
        this.cancelBg = cancelBg;
        return this;
    }

    /**
     * 设置确认按钮文字
     */
    public LcaiTipsDialogBuilder setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    /**
     * 设置确认按钮文字颜色 (@ColorInt)
     */
    public LcaiTipsDialogBuilder setConfirmColor(int confirmColor) {
        this.confirmColor = confirmColor;
        return this;
    }

    /**
     * 设置确认按钮文字大小 (sp)
     */
    public LcaiTipsDialogBuilder setConfirmSize(int confirmSize) {
        this.confirmSize = confirmSize;
        return this;
    }

    /**
     * 设置确认按钮背景
     */
    public LcaiTipsDialogBuilder setConfirmBg(Drawable confirmBg) {
        this.confirmBg = confirmBg;
        return this;
    }

    /**
     * 设置是否允许点击外部关闭
     * 注意：原字段名 outSideCancal 疑似拼写错误，此处保持与原字段一致
     */
    public LcaiTipsDialogBuilder setOutSideCancal(boolean outSideCancal) {
        this.outSideCancal = outSideCancal;
        return this;
    }

    /**
     * 设置点击回调接口
     */
    public LcaiTipsDialogBuilder setOnTipsDialogInterface(OnTipsDialogInterface dialogInterface) {
        this.dialogInterface = dialogInterface;
        return this;
    }

    /**
     * 管不弹窗
     */
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            release();
        }
    }

    public void release() {
        this.mContext = null;
    }

    public void build() {
        dialog=new LcaiTipsDialog(this);
    }


}
