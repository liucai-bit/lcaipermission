package com.liucai.tipsdialog.bulider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.liucai.component.base.BaseRecycleAdapter;
import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.base.LcaiActivityManager;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.http.thread.GlobalThreadPool;
import com.liucai.tipsdialog.core.LcaiTipsDialog;
import com.liucai.tipsdialog.core.LcaiTipsMode;
import com.liucai.tipsdialog.core.OnTipsDialogInterface;
import com.liucai.tipsdialog.module.SegDisplayModule;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用弹窗 Builder，支持 7 种弹窗模式，线程安全，自动管理宿主与生命周期。
 *
 * @author liucai
 * @program lcpermission
 * @date 2026/5/27
 */
@SuppressWarnings("unused")
public class LcaiTipsDialogBuilder<T extends BaseRecycleAdapter<?,?>> {
    /**
     * 弹窗对象
     */
    @Nullable
    private LcaiTipsDialog dialog;
    /**
     * 上下文对象
     */
    @Nullable
    public Context mContext;
    /**
     * 主线程 Handler
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * 弹窗模式
     */
    public LcaiTipsMode mode = LcaiTipsMode.DEFALUT_MODE;
    /**
     * 自定义弹窗 layout
     */
    public int customLayout;
    /**
     * 弹窗背景颜色 / Drawable
     */
    public Drawable popupBg;
    /**
     * 弹窗标题
     */
    public String title;
    /**
     * 标题文字颜色
     */
    public int titleColor = Color.parseColor("#1C1C1C");
    /**
     * 标题文字大小 (sp)
     */
    public int titleSize = 18;
    /**
     * LIST 模式适配器
     */
    public T adapter;
    /**
     * 列表是否纵向模式
     */
    public boolean vertical = true;
    /**
     * 列表横向每行数据数量
     */
    public int listCloumns;
    /**
     * 弹窗内容
     */
    public String content;
    /**
     * 内容文字颜色
     */
    public int contentColor = Color.parseColor("#1C1C1C");
    /**
     * 内容文字大小 (sp)
     */
    public int contentSize = 16;
    /**
     * 点击内容是否关闭
     */
    public boolean contentClickDismiss;
    /**
     * 是否分段显示（用于富文本协议）
     */
    public boolean isSegDisplay;
    /**
     * 分段显示内容列表
     */
    public List<SegDisplayModule> moduleList;
    /**
     * 内容居中方式
     */
    public int contentGravity;
    /**
     * 取消按钮文字
     */
    public String cancelText;
    /**
     * 取消按钮文字颜色
     */
    @ColorInt
    public int cancelColor = Color.parseColor("#1C1C1C");
    /**
     * 取消按钮文字大小 (sp)
     */
    public int cancelSize = 16;
    /**
     * 取消按钮背景
     */
    public Drawable cancelBg;
    /**
     * 确认按钮文字
     */
    public String confirmText;
    /**
     * 确认按钮文字颜色
     */
    public int confirmColor = Color.parseColor("#1C1C1C");
    /**
     * 确认按钮文字大小 (sp)
     */
    public int confirmSize = 16;
    /**
     * 确认按钮背景
     */
    public Drawable confirmBg;
    /**
     * 是否允许点击外部关闭（原字段名保留拼写错误以兼容）
     */
    public boolean outSideCancal;
    /**
     * 点击回调接口
     */
    public OnTipsDialogInterface dialogInterface;

    // ==================== 弹窗基础属性 ====================

    /**
     * 设置弹窗模式
     */
    public LcaiTipsDialogBuilder<T> setMode(LcaiTipsMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * 设置自定义弹窗内容（仅 CUSTOM_MODE 使用）
     */
    public LcaiTipsDialogBuilder<T> setCustomLayout(int customLayout) {
        this.customLayout = customLayout;
        return this;
    }

    /**
     * 设置弹窗背景
     */
    public LcaiTipsDialogBuilder<T> setPopupBg(Drawable popupBg) {
        this.popupBg = popupBg;
        return this;
    }

    /**
     * 设置弹窗背景（资源）
     */
    public LcaiTipsDialogBuilder<T> setPopupBgRes(@DrawableRes int resId) {
        this.popupBg = GlobalAppUtil.resolveDrawable(mContext, resId);
        return this;
    }

    // ==================== 标题 ====================

    /**
     * 设置标题文字
     */
    public LcaiTipsDialogBuilder<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置标题文字（字符串资源）
     */
    public LcaiTipsDialogBuilder<T> setTitleRes(@StringRes int resId) {
        this.title = GlobalAppUtil.resolveString(mContext, resId);
        return this;
    }

    /**
     * 设置标题文字颜色
     */
    public LcaiTipsDialogBuilder<T> setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    /**
     * 设置标题文字颜色（颜色资源）
     */
    public LcaiTipsDialogBuilder<T> setTitleColorRes(@ColorRes int resId) {
        this.titleColor = GlobalAppUtil.resolveColor(mContext, resId);
        return this;
    }

    /**
     * 设置标题文字大小 (sp)
     */
    public LcaiTipsDialogBuilder<T> setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    // ==================== 列表 ====================

    /**
     * 设置列表适配器
     */
    public LcaiTipsDialogBuilder<T> setAdapter(T adapter) {
        this.adapter = adapter;
        return this;
    }

    /**
     * 设置列表是否纵向排列
     */
    public LcaiTipsDialogBuilder<T> setListVertical(boolean vertical) {
        this.vertical = vertical;
        return this;
    }

    /**
     * 设置列表横向每行数据数量
     */
    public LcaiTipsDialogBuilder<T> setListCloumns(int cloumns) {
        this.listCloumns = cloumns;
        return this;
    }

    // ==================== 内容 ====================

    /**
     * 设置内容文字
     */
    public LcaiTipsDialogBuilder<T> setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 设置内容文字（字符串资源）
     */
    public LcaiTipsDialogBuilder<T> setContentRes(@StringRes int resId) {
        this.content = GlobalAppUtil.resolveString(mContext, resId);
        return this;
    }

    /**
     * 设置内容文字颜色
     */
    public LcaiTipsDialogBuilder<T> setContentColor(int contentColor) {
        this.contentColor = contentColor;
        return this;
    }

    /**
     * 设置内容文字颜色（颜色资源）
     */
    public LcaiTipsDialogBuilder<T> setContentColorRes(@ColorRes int resId) {
        this.contentColor = GlobalAppUtil.resolveColor(mContext, resId);
        return this;
    }

    /**
     * 设置内容文字大小 (sp)
     */
    public LcaiTipsDialogBuilder<T> setContentSize(int contentSize) {
        this.contentSize = contentSize;
        return this;
    }

    /**
     * 设置点击内容是否关闭弹窗
     */
    public LcaiTipsDialogBuilder<T> setContentClickDismiss(boolean contentClickDismiss) {
        this.contentClickDismiss = contentClickDismiss;
        return this;
    }

    /**
     * 设置是否分段显示（用于富文本协议）
     */
    public LcaiTipsDialogBuilder<T> setSegDisplay(boolean isSegDisplay) {
        this.isSegDisplay = isSegDisplay;
        return this;
    }

    /**
     * 设置分段显示内容列表
     */
    public LcaiTipsDialogBuilder<T> setModuleList(List<SegDisplayModule> moduleList) {
        this.moduleList = moduleList;
        return this;
    }

    /**
     * 添加单个分段显示模块
     */
    public LcaiTipsDialogBuilder<T> addModule(SegDisplayModule module) {
        if (this.moduleList == null) {
            this.moduleList = new ArrayList<>();
        }
        this.moduleList.add(module);
        return this;
    }

    /**
     * 设置内容居中方式
     */
    public LcaiTipsDialogBuilder<T> setContentGravity(int contentGravity) {
        this.contentGravity = contentGravity;
        return this;
    }

    // ==================== 取消按钮 ====================

    /**
     * 设置取消按钮文字
     */
    public LcaiTipsDialogBuilder<T> setCancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    /**
     * 设置取消按钮文字（字符串资源）
     */
    public LcaiTipsDialogBuilder<T> setCancelTextRes(@StringRes int resId) {
        this.cancelText = GlobalAppUtil.resolveString(mContext, resId);
        return this;
    }

    /**
     * 设置取消按钮文字颜色
     */
    public LcaiTipsDialogBuilder<T> setCancelColor(int cancelColor) {
        this.cancelColor = cancelColor;
        return this;
    }

    /**
     * 设置取消按钮文字颜色（颜色资源）
     */
    public LcaiTipsDialogBuilder<T> setCancelColorRes(@ColorRes int resId) {
        this.cancelColor = GlobalAppUtil.resolveColor(mContext, resId);
        return this;
    }

    /**
     * 设置取消按钮文字大小 (sp)
     */
    public LcaiTipsDialogBuilder<T> setCancelSize(int cancelSize) {
        this.cancelSize = cancelSize;
        return this;
    }

    /**
     * 设置取消按钮背景
     */
    public LcaiTipsDialogBuilder<T> setCancelBg(Drawable cancelBg) {
        this.cancelBg = cancelBg;
        return this;
    }

    /**
     * 设置取消按钮背景（资源）
     */
    public LcaiTipsDialogBuilder<T> setCancelBgRes(@DrawableRes int resId) {
        this.cancelBg = GlobalAppUtil.resolveDrawable(mContext, resId);
        return this;
    }

    // ==================== 确认按钮 ====================

    /**
     * 设置确认按钮文字
     */
    public LcaiTipsDialogBuilder<T> setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    /**
     * 设置确认按钮文字（字符串资源）
     */
    public LcaiTipsDialogBuilder<T> setConfirmTextRes(@StringRes int resId) {
        this.confirmText = GlobalAppUtil.resolveString(mContext, resId);
        return this;
    }

    /**
     * 设置确认按钮文字颜色
     */
    public LcaiTipsDialogBuilder<T> setConfirmColor(int confirmColor) {
        this.confirmColor = confirmColor;
        return this;
    }

    /**
     * 设置确认按钮文字颜色（颜色资源）
     */
    public LcaiTipsDialogBuilder<T> setConfirmColorRes(@ColorRes int resId) {
        this.confirmColor = GlobalAppUtil.resolveColor(mContext, resId);
        return this;
    }

    /**
     * 设置确认按钮文字大小 (sp)
     */
    public LcaiTipsDialogBuilder<T> setConfirmSize(int confirmSize) {
        this.confirmSize = confirmSize;
        return this;
    }

    /**
     * 设置确认按钮背景
     */
    public LcaiTipsDialogBuilder<T> setConfirmBg(Drawable confirmBg) {
        this.confirmBg = confirmBg;
        return this;
    }

    /**
     * 设置确认按钮背景（资源）
     */
    public LcaiTipsDialogBuilder<T> setConfirmBgRes(@DrawableRes int resId) {
        this.confirmBg = GlobalAppUtil.resolveDrawable(mContext, resId);
        return this;
    }

    // ==================== 行为与回调 ====================

    /**
     * 设置是否允许点击外部关闭
     * <p>注意：原字段名 outSideCancal 疑似拼写错误，此处保持与原字段一致
     */
    public LcaiTipsDialogBuilder<T> setOutSideCancal(boolean outSideCancal) {
        this.outSideCancal = outSideCancal;
        return this;
    }

    /**
     * 设置点击回调接口
     */
    public LcaiTipsDialogBuilder<T> setOnTipsDialogInterface(OnTipsDialogInterface dialogInterface) {
        this.dialogInterface = dialogInterface;
        return this;
    }

    /**
     * 显式指定宿主（可选）。
     * <p>不调用此方法时，{@link #build()} 会自动从 {@link LcaiActivityManager} 获取
     * 当前前台可见的 Activity。大多数场景无需调用本方法。
     */
    public LcaiTipsDialogBuilder<T> setContext(@NonNull Context context) {
        this.mContext = context;
        return this;
    }

    // ==================== 显示与关闭 ====================

    /**
     * 显示弹窗（宿主无效时抛异常）。
     * <p>适合开发期：快速暴露"在错误的时机弹窗"问题。
     * <p>可在任意线程调用，内部自动切主线程。
     *
     * @throws IllegalStateException 宿主不可用
     */
    public void showOrThrow() {
        if (isMainThread()) {
            Activity host = resolveHost();
            if (host == null) {
                throw new IllegalStateException(
                        "No resumed activity available to host the tips dialog");
            }
            dismissQuietly();
            this.mContext = host;
            dialog = new LcaiTipsDialog(this);
            return;
        }
        mainHandler.post(this::showOrThrow);

    }

    /**
     * 关闭弹窗。可安全重复调用。
     * <p>可在任意线程调用，内部自动切主线程。
     */
    public void dismiss() {
        if (isMainThread()) {
            dismissQuietly();
            return;
        }
        mainHandler.post(this::dismiss);
    }

    /**
     * 是否正在显示
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
     * <p>1. 调用方显式设置的 Activity 且有效 → 用它
     * <p>2. 从 LcaiActivityManager 取前台可见 Activity 且有效 → 用它
     * <p>3. 都无效 → null
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

    /**
     * 静默关闭弹窗。宿主已销毁时可能抛异常，统一捕获。
     */
    private void dismissQuietly() {
        LcaiTipsDialog d = dialog;
        dialog = null;
        if (d != null) {
            try {
                if (d.isShowing()) {
                    d.dismiss();
                }
            } catch (Throwable t) {
                LcaiLogUtils.d("TipsDialog dismiss failed: " + t.getMessage());
            }
        }
    }

    /**
     * 是否处于主线程
     */
    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 显示弹窗。
     * <p>宿主无效时静默失败（不抛异常），适合生产环境。
     * <p>可在任意线程调用，内部自动切主线程。
     */
    public void build() {
        if (isMainThread()) {
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
                dialog.setOnDismissListener(d -> release());
                dialog.show();
            } catch (Throwable t) {
                LcaiLogUtils.d("TipsDialog build failed: " + t.getMessage());
                dialog = null;
            }
            return;
        }
        mainHandler.post(this::build);
    }

    /**
     * LOADING 场景推荐入口。
     * <p>自动完成：显示 loading → 等一帧 → 子线程执行 task →
     * 回主线程关闭 dialog → 回调 onBuildAsync。
     * <p>全程主线程不阻塞，loading 动画可持续旋转。
     * <p>task 内部抛出的异常会通过 {@link OnTipsDialogInterface#onBuildAsync}
     * 的 error 参数回传（{@code null} 表示成功）。
     */
    public void buildAsync(@NonNull Runnable task) {
        if (isMainThread()) {
            // 1) 显示 loading（内部会 dismiss 旧的 + 校验宿主）
            build();
            // 2) 宿主无效：任务照跑（不丢数据），但没 loading 可关
            if (!isShowing()) {
                LcaiLogUtils.d("TipsDialog buildAsync: no valid host, run task directly");
                mainHandler.postDelayed(() -> startAsyncTask(task), 0);
                return;
            }

            // 3) 宿主有效：等 loading 至少绘制一帧后再启动任务，
            //    避免主线程同步阻塞导致 loading 动画冻结
            if (dialog != null) {
                View decor = dialog.getWindow() != null ? dialog.getWindow().getDecorView() : null;
                if (decor == null) {
                    mainHandler.postDelayed(() -> startAsyncTask(task), 100);   // 降级路径
                } else {
                    decor.postOnAnimation(() ->
                            decor.postOnAnimation(() ->
                                    mainHandler.post(() -> startAsyncTask(task))));
                }
            }

            return;
        }
        mainHandler.post(() -> buildAsync(task));
    }

    /**
     * 子线程执行 task，完成后回主线程回调并关闭 loading。
     */
    private void startAsyncTask(@NonNull Runnable task) {
        GlobalThreadPool.execute(() -> {
            Throwable error = null;
            try {
                task.run();
            } catch (Throwable t) {
                error = t;
                LcaiLogUtils.e("TipsDialog async task failed", t);
            }
            final Throwable fe = error;

            // 回主线程：先回调、再 dismiss，保证回调能拿到 dialogInterface
            mainHandler.post(() -> {
                if (dialogInterface != null) {
                    dialogInterface.onBuildAsync(this, fe);
                }
                dismissQuietly();
            });
        });
    }
}