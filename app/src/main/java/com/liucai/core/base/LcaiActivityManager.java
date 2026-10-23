package com.liucai.core.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

    private final CopyOnWriteArrayList<Activity> activityList = new CopyOnWriteArrayList<>();

    private volatile Activity mResumedActivity;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LoadingAction sPending;

    private ActiveLoading sActive;

    private volatile boolean initialized;

    public void init(@NonNull Application application) {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            application.registerActivityLifecycleCallbacks(this);
            initialized = true;
        }
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

        ActiveLoading active = sActive;
        if (active != null && active.host == activity) {
            sActive = null;
            try {
                active.action.dismiss();
            } catch (Throwable t) {
                LcaiLogUtils.d("dismiss loading on destroy failed: " + t.getMessage());
            }
        }

        LcaiLogUtils.d(activity.getClass().getSimpleName() + ", StackSize: " + activityList.size());
    }

    // ==================== 栈查询 ====================

    @Nullable
    public Activity currentActivity() {
        int size = activityList.size();
        if (size == 0) {
            return null;
        }
        return activityList.get(size - 1);
    }

    @NonNull
    public List<Activity> getAllActivities() {
        return new ArrayList<>(activityList);
    }

    public boolean hasActivity() {
        return !activityList.isEmpty();
    }

    // ==================== 可见性查询 ====================

    @Nullable
    public Activity getResumedActivity() {
        return mResumedActivity;
    }

    public boolean isResumed(@Nullable Activity activity) {
        return activity != null
                && activity == mResumedActivity
                && isAlive(activity);
    }

    public boolean isAppForeground() {
        return isAlive(mResumedActivity);
    }

    // ==================== Loading 封装 ====================

    public interface LoadingAction {
        void show(@NonNull Activity host);

        void dismiss();
    }

    private static final class ActiveLoading {
        final LoadingAction action;
        final Activity host;

        ActiveLoading(LoadingAction action, Activity host) {
            this.action = action;
            this.host = host;
        }
    }

    public void showLoading(@NonNull LoadingAction action) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> showLoading(action));
            return;
        }

        Activity host = mResumedActivity;
        if (!isAlive(host)) {
            sPending = action;
            LcaiLogUtils.d("showLoading: no resumed activity, pending");
            return;
        }

        dismissActiveInternal();

        try {
            action.show(host);
            sActive = new ActiveLoading(action, host);
        } catch (Throwable t) {
            LcaiLogUtils.d("showLoading failed: " + t.getMessage());
        }
    }

    public void dismissLoading() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(this::dismissLoading);
            return;
        }
        sPending = null;
        dismissActiveInternal();
    }

    public boolean isLoadingShowing() {
        return sActive != null;
    }

    private void dismissActiveInternal() {
        ActiveLoading active = sActive;
        sActive = null;
        if (active != null) {
            try {
                active.action.dismiss();
            } catch (Throwable t) {
                LcaiLogUtils.d("dismissLoading failed: " + t.getMessage());
            }
        }
    }

    private void flushPendingLoading() {
        LoadingAction pending = sPending;
        if (pending == null) {
            return;
        }

        sPending = null;

        Activity host = mResumedActivity;
        if (!isAlive(host)) {
            return;
        }

        dismissActiveInternal();

        try {
            pending.show(host);
            sActive = new ActiveLoading(pending, host);
        } catch (Throwable t) {
            LcaiLogUtils.d("flush pending loading failed: " + t.getMessage());
        }
    }

    // ==================== 关闭 Activity / 跳转 ====================

    public void finishFirstActivity(@Nullable Activity activity) {
        if (isAlive(activity)) {
            activity.finish();
        }
    }

    public void finishAllActivities(@Nullable Class<?> cls) {
        for (Activity activity : new ArrayList<>(activityList)) {
            if (cls == null || activity.getClass().equals(cls)) {
                finishFirstActivity(activity);
            }
        }
    }

    public void finishAllOtherActivities(@NonNull Class<?> cls) {
        for (Activity activity : new ArrayList<>(activityList)) {
            if (!activity.getClass().equals(cls)) {
                finishFirstActivity(activity);
            }
        }
    }

    public void finishAllActivities() {
        finishAllActivities(null);
    }

    public void exitApp() {
        finishAllActivities();
        Process.killProcess(Process.myPid());
    }

    public void startActivity(@NonNull Context context, @NonNull Class<?> targetClass) {
        startActivity(context, targetClass, null);
    }

    public void startActivity(@NonNull Context context,
                              @NonNull Class<?> targetClass,
                              @Nullable Bundle bundle) {
        if (context instanceof Activity) {
            Activity host = (Activity) context;
            if (!isAlive(host)) {
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

    private static boolean isAlive(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        }
        return true;
    }
}