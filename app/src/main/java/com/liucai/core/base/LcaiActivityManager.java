package com.liucai.core.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局 Activity 生命周期管理器。
 *
 * <p>职责：
 * <ul>
 *   <li>维护全量 Activity 栈（创建顺序，尾部为最后创建）</li>
 *   <li>维护"前台可见（resumed）Activity"，作为弹 Dialog / Loading 的宿主来源</li>
 *   <li>提供 Loading 的显示 / 关闭 / 跳转空窗期暂存能力</li>
 *   <li>提供关闭指定 / 全部 Activity、退出 App、页面跳转等通用能力</li>
 * </ul>
 *
 * <p>使用前必须在 {@link Application#onCreate()} 中调用 {@link #init(Application)}。
 *
 * <p>注意：
 * <ul>
 *   <li>{@link #currentActivity()} 返回栈顶（不保证前台可见），弹窗请优先用
 *       {@link #getResumedActivity()} 或 {@link #showLoading(LoadingAction)}</li>
 *   <li>{@link #getAllActivities()} 返回只读快照，外部修改不影响内部状态</li>
 *   <li>生命周期回调均在主线程，{@link #showLoading(LoadingAction)} /
 *       {@link #dismissLoading()} 支持跨线程调用，内部自动切主线程</li>
 * </ul>
 *
 * @author liucai
 */
public class LcaiActivityManager implements Application.ActivityLifecycleCallbacks {

    private static final class Holder {
        static final LcaiActivityManager INSTANCE = new LcaiActivityManager();
    }

    public static LcaiActivityManager getInstance() {
        return Holder.INSTANCE;
    }

    private LcaiActivityManager() {
    }

    /**
     * 全量 Activity 列表，按创建顺序追加，尾部为最后创建。
     * <p>写操作仅在主线程（生命周期回调），COWAL 保证跨线程读的可见性与安全性。
     */
    private final CopyOnWriteArrayList<Activity> activityList = new CopyOnWriteArrayList<>();

    /**
     * 当前处于 resumed（前台可见）状态的 Activity。
     * <p>volatile：允许任意线程读取 {@link #isAppForeground()} 等查询。
     */
    private volatile Activity mResumedActivity;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 等待下一个 resumed 宿主弹出的 Loading（仅主线程访问）。 */
    private LoadingAction sPending;

    /** 当前已弹出的 Loading（volatile：供任意线程查询是否显示）。 */
    private volatile LoadingAction sActive;

    public void init(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        activityList.add(activity);
        LcaiLogUtils.d(activity.getClass().getSimpleName() + ", StackSize: " + activityList.size());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // no-op
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mResumedActivity = activity;
        // 跳转空窗期暂存的 Loading，在有可见宿主后补弹
        flushPendingLoading();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (mResumedActivity == activity) {
            mResumedActivity = null;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // no-op
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // no-op
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        activityList.remove(activity);
        if (mResumedActivity == activity) {
            mResumedActivity = null;
        }
        // 兜底：宿主销毁时主动关闭 Loading，避免泄漏
        if (sActive != null) {
            try {
                sActive.dismiss();
            } catch (Throwable t) {
                LcaiLogUtils.d("dismiss loading on destroy failed: " + t.getMessage());
            }
            sActive = null;
        }
        LcaiLogUtils.d(activity.getClass().getSimpleName() + ", StackSize: " + activityList.size());
    }

    // ==================== 栈查询 ====================

    /**
     * 获取栈顶 Activity（最后创建，不保证前台可见）。
     * <p>弹 Dialog / Loading 请优先使用 {@link #getResumedActivity()}。
     */
    @Nullable
    public Activity currentActivity() {
        int size = activityList.size();
        if (size == 0) {
            return null;
        }
        return activityList.get(size - 1);
    }

    /**
     * 获取所有 Activity 的只读快照。外部修改快照不影响内部状态。
     */
    @NonNull
    public List<Activity> getAllActivities() {
        return new ArrayList<>(activityList);
    }

    /**
     * 是否还有存活的 Activity。
     */
    public boolean hasActivity() {
        return !activityList.isEmpty();
    }

    // ==================== 可见性查询 ====================

    /**
     * 获取当前前台可见（resumed）的 Activity。可能为 null（后台、跳转空窗期等）。
     * <p>这是弹 Dialog / Loading 时最应该用的入口。
     */
    @Nullable
    public Activity getResumedActivity() {
        return mResumedActivity;
    }

    /**
     * 给定 Activity 是否处于前台可见且未销毁状态。
     */
    public boolean isResumed(@Nullable Activity activity) {
        return activity != null
                && activity == mResumedActivity
                && !activity.isFinishing()
                && !activity.isDestroyed();
    }

    /**
     * App 是否在前台（有可见且未销毁的 Activity）。
     */
    public boolean isAppForeground() {
        return mResumedActivity != null
                && !mResumedActivity.isFinishing()
                && !mResumedActivity.isDestroyed();
    }

    // ==================== Loading 封装 ====================

    /**
     * Loading 动作抽象。
     * <p>实现方在 {@link #show(Activity)} 中把 UI 挂到 host 上（Dialog / DecorView addView / 等），
     * 在 {@link #dismiss()} 中移除。
     */
    public interface LoadingAction {
        void show(@NonNull Activity host);

        void dismiss();
    }

    /**
     * 显示 Loading。
     * <p>若当前无前台可见 Activity，则暂存请求，待下一个 Activity resumed 时自动补弹。
     * <p>可在任意线程调用，内部自动切主线程。
     */
    public void showLoading(@NonNull LoadingAction action) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> showLoading(action));
            return;
        }
        Activity host = mResumedActivity;
        if (host == null || host.isFinishing() || host.isDestroyed()) {
            sPending = action;
            LcaiLogUtils.d("showLoading: no resumed activity, pending");
            return;
        }
        // 先清旧，避免叠加
        dismissActiveInternal();
        action.show(host);
        sActive = action;
    }

    /**
     * 关闭当前 Loading（若存在）。同时清理等待中的 pending。
     * <p>可在任意线程调用，内部自动切主线程。
     */
    public void dismissLoading() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::dismissLoading);
            return;
        }
        sPending = null;
        dismissActiveInternal();
    }

    /**
     * 是否正在显示 Loading。
     */
    public boolean isLoadingShowing() {
        return sActive != null;
    }

    /** 主线程内部调用：关闭当前 active 并清空。 */
    private void dismissActiveInternal() {
        LoadingAction active = sActive;
        sActive = null;
        if (active != null) {
            try {
                active.dismiss();
            } catch (Throwable t) {
                LcaiLogUtils.d("dismissLoading failed: " + t.getMessage());
            }
        }
    }

    /** 主线程内部调用：resumed 时补弹 pending。 */
    private void flushPendingLoading() {
        LoadingAction pending = sPending;
        if (pending == null) {
            return;
        }
        sPending = null;
        Activity host = mResumedActivity;
        if (host == null || host.isFinishing() || host.isDestroyed()) {
            return;
        }
        dismissActiveInternal();
        try {
            pending.show(host);
            sActive = pending;
        } catch (Throwable t) {
            LcaiLogUtils.d("flush pending loading failed: " + t.getMessage());
        }
    }

    /**
     * 关闭指定的 Activity。
     */
    public void finishFirstActivity(@Nullable Activity activity) {
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            activity.finish();
        }
    }

    /**
     * 关闭所有指定类名的 Activity。cls 为 null 表示关闭全部。
     * <p>遍历使用快照，避免 finish 触发的 onDestroyed 导致并发修改。
     */
    public void finishAllActivities(@Nullable Class<?> cls) {
        for (Activity activity : new ArrayList<>(activityList)) {
            if (cls == null || activity.getClass().equals(cls)) {
                finishFirstActivity(activity);
            }
        }
    }

    /**
     * 关闭除指定类名外的所有 Activity。
     */
    public void finishAllOtherActivities(@NonNull Class<?> cls) {
        for (Activity activity : new ArrayList<>(activityList)) {
            if (!activity.getClass().equals(cls)) {
                finishFirstActivity(activity);
            }
        }
    }

    /**
     * 关闭所有 Activity。
     */
    public void finishAllActivities() {
        finishAllActivities(null);
    }

    /**
     * 退出 App：关闭所有 Activity 后杀进程。
     */
    public void exitApp() {
        finishAllActivities();
        Process.killProcess(Process.myPid());
    }

    public void startActivity(@NonNull Context context, @NonNull Class<?> targetClass) {
        startActivity(context, targetClass, null);
    }

    public void startActivity(@NonNull Context context, @NonNull Class<?> targetClass, @Nullable Bundle bundle) {
        // context 为已销毁/正在结束的 Activity 时直接丢弃，避免 startActivity 崩溃
        if (context instanceof Activity) {
            Activity host = (Activity) context;
            if (host.isFinishing() || host.isDestroyed()) {
                LcaiLogUtils.d("startActivity: context activity is finishing/destroyed");
                return;
            }
        }
        Intent intent = new Intent(context, targetClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}