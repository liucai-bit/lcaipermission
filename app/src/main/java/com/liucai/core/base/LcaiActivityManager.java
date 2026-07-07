package com.liucai.core.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

import java.util.Stack;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/9
 */
public class LcaiActivityManager implements Application.ActivityLifecycleCallbacks {

    public static LcaiActivityManager instance;

    private static Stack<LcaiBaseActivity> activityStack = new Stack<>();

    private static final class ActivityManager{
        public static final LcaiActivityManager INSTANCE = new LcaiActivityManager();
    }

    private LcaiActivityManager() {
    }

    public static LcaiActivityManager getInstance() {
        return ActivityManager.INSTANCE;
    }

    public void init(LcaiBaseApplication application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        activityStack.push((LcaiBaseActivity) activity);
        LcaiLogUtils.d( activity.getClass().getSimpleName() + ", StackSize: " + activityStack.size());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activityStack.contains(activity)) {
            activityStack.remove(activity);
            LcaiLogUtils.d(activity.getClass().getSimpleName() + ", StackSize: " + activityStack.size());
        }
    }

    /**
     * 获取当前正在显示的 Activity (栈顶)
     */
    public Activity currentActivity() {
        if (activityStack.isEmpty()) {
            return null;
        }
        return activityStack.lastElement();
    }

    /**
     * 获取栈中所有 Activity (只读副本，防止外部修改导致并发问题)
     */
    public Stack<Activity> getAllActivities() {
        // 返回克隆对象，避免外部直接操作原始栈导致线程安全问题
        return (Stack<Activity>) activityStack.clone();
    }

    /**
     * 关闭指定的 Activity
     * @param activity 要关闭的 Activity 实例
     */
    public void finishActivity(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
            // 注意：finish() 是异步的，onActivityDestroyed 会在稍后回调中执行移除操作
            // 如果需要同步从栈中移除以立即生效，可以手动 remove，但通常建议依赖生命周期回调
        }
    }

    /**
     * 关闭指定类名的 Activity
     * @param cls Activity 的 Class
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
                break; // 通常一个类名只有一个实例，如果有多个需遍历全部
            }
        }
    }

    /**
     * 关闭除了指定类名以外的所有 Activity
     * @param cls 保留的 Activity 类名
     */
    public void finishAllOtherActivities(Class<?> cls) {
        // 必须使用迭代器或复制列表，因为我们在遍历过程中会修改集合状态(通过finish触发后续移除)
        // 这里为了安全，先复制一份列表
        Stack<Activity> copyStack = (Stack<Activity>) activityStack.clone();
        for (Activity activity : copyStack) {
            if (!activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 关闭所有 Activity
     */
    public void finishAllActivities() {
        Stack<Activity> copyStack = (Stack<Activity>) activityStack.clone();
        for (Activity activity : copyStack) {
            finishActivity(activity);
        }
    }

    /**
     * 退出 App
     * 1. 关闭所有 Activity
     * 2. 杀死进程
     */
    public void exitApp() {
        finishAllActivities();

        // 杀死进程
        android.os.Process.killProcess(android.os.Process.myPid());
        // 或者使用 System.exit(0);

        // 注意：在某些高版本 Android 或特定 ROM 上，killProcess 可能不会立即生效，
        // 也可以结合 moveTaskToBack(true) 使用，但 killProcess 是最彻底的退出方式之一
    }

    /**
     * 简单的页面跳转封装 (可选)
     * @param context 上下文
     * @param targetClass 目标 Activity
     */
    public void startActivity(Context context, Class<?> targetClass) {
        Intent intent = new Intent(context, targetClass);
        // 如果 context 不是 Activity，需要添加 FLAG_ACTIVITY_NEW_TASK
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 带参数的页面跳转
     */
    public void startActivity(Context context, Class<?> targetClass, Bundle bundle) {
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
