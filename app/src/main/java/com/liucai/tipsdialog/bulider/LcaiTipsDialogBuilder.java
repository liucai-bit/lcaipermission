package com.liucai.tipsdialog.bulider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.base.LcaiActivityManager;
import com.liucai.core.util.log.LcaiLogUtils;
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
    @Nullable
    private LcaiTipsDialog dialog;
    /** 上下文对象 */
    @Nullable
    public Context mContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
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
     * 显式指定宿主（<b>可选</b>）。
     * <p>不调用此方法时，{@link #build()} 会自动从 {@link LcaiActivityManager} 获取
     * 当前前台可见的 Activity。大多数场景无需调用本方法。
     */
    public LcaiTipsDialogBuilder<T> setContext(@NonNull Context context) {
        this.mContext = context;
        return this;
    }

    /**
     * 显示弹窗（宿主无效时抛异常）。
     *
     * <p>适合开发期：快速暴露"在错误的时机弹窗"问题。
     * <p>可在任意线程调用，内部自动切主线程。
     *
     * @throws IllegalStateException 宿主不可用
     */
    public void showOrThrow() {
        if (!isMainThread()) {
            mainHandler.post(this::showOrThrow);
            return;
        }
        Activity host = resolveHost();
        if (host == null) {
            throw new IllegalStateException(
                    "No resumed activity available to host the tips dialog");
        }
        dismissQuietly();
        this.mContext = host;
        dialog = new LcaiTipsDialog(this);
    }

    // ==================== 关闭 ====================

    /**
     * 关闭弹窗。可安全重复调用。
     * <p>可在任意线程调用，内部自动切主线程。
     */
    public void dismiss() {
        if (!isMainThread()) {
            mainHandler.post(this::dismiss);
            return;
        }
        dismissQuietly();
    }

    /**
     * 是否正在显示。
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    /**
     * 彻底释放 Builder 持有的外部引用。
     * <p>Builder 用完后调用，调用后本实例不应再被复用。
     */
    public void release() {
        if (dialog != null) {
            dialog.builder = null;
            dialog = null;
        }
        dismissQuietly();
        mContext = null;
        adapter = null;
        dialogInterface = null;
        moduleList = null;
        popupBg = null;
        cancelBg = null;
        confirmBg = null;
    }

    // ==================== 内部实现 ====================

    /**
     * 解析宿主：
     * 1. 调用方显式设置的 Activity 且有效 → 用它
     * 2. 从 LcaiActivityManager 取前台可见 Activity 且有效 → 用它
     * 3. 都无效 → null
     */
    @Nullable
    private Activity resolveHost() {
        if (mContext instanceof Activity) {
            Activity a = (Activity) mContext;
            if (!a.isFinishing() && !a.isDestroyed()) {
                return a;
            }
        }
        Activity resumed = LcaiActivityManager.getInstance().getResumedActivity();
        if (resumed != null && !resumed.isFinishing() && !resumed.isDestroyed()) {
            return resumed;
        }
        return null;
    }

    private void dismissQuietly() {
        LcaiTipsDialog d = dialog;
        dialog = null;
        if (d != null) {
            try {
                if (d.isShowing()) {
                    d.dismiss();
                }
            } catch (Throwable t) {
                // 宿主已销毁时 dismiss 可能抛异常，忽略
                LcaiLogUtils.d("TipsDialog dismiss failed: " + t.getMessage());
            }
        }
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
    /**
     * 显示弹窗。
     *
     * <p>宿主无效时<b>静默失败</b>并返回 {@code false}，不抛异常，适合生产环境。
     * <p>可在任意线程调用，内部自动切主线程。
     *
     * @return 是否成功显示
     */
    public void build() {
        if (!isMainThread()) {
            mainHandler.post(this::build);
            return;
        }
        dismissQuietly();
        Activity host = resolveHost();
        if (host == null) {
            LcaiLogUtils.d("TipsDialog show skipped: no valid host");
            return;
        }
        // 若已有 dialog 正在显示，先关掉，避免叠弹
        dismissQuietly();
        // 把最终宿主回写 mContext，供 LcaiTipsDialog 内部使用
        this.mContext = host;
        try {
            dialog = new LcaiTipsDialog(this);
            dialog.setOnDismissListener(d -> {
                release();
            });
            dialog.show();
        } catch (Throwable t) {
            LcaiLogUtils.d("TipsDialog build failed: " + t.getMessage());
            dialog = null;
        }
    }


}
