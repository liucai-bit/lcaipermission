package com.liucai.eventBus;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier; // 用于过滤 static 方法
import java.util.ArrayList;
import java.util.HashMap; // 优化：使用 HashMap 替代 ConcurrentHashMap 以提升性能
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author liucai
 * @description 轻量型基于事件ID的事件总线
 */
public class LcaiEventBus {
    private static volatile LcaiEventBus instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Key为事件ID，Value为该事件下所有订阅者的集合
    private final Map<String, CopyOnWriteArraySet<Subscription>> eventMap = new HashMap<>();

    // 粘性事件缓存
    private final Map<String, MessageEvent> stickyEventMap = new ConcurrentHashMap<>();

    private LcaiEventBus() {
    }

    public static LcaiEventBus getInstance() {
        if (instance == null) {
            synchronized (LcaiEventBus.class) {
                if (instance == null) {
                    instance = new LcaiEventBus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册订阅
     * 支持向上遍历父类查找 @Subscribe 方法
     */
    public void register(@NonNull String eventId, @NonNull Object subscriber) {
        if (TextUtils.isEmpty(eventId) || subscriber == null) {
            throw new IllegalArgumentException("eventId and subscriber can not be empty");
        }

        // 1. 获取所有方法（包含父类）
        List<Method> allMethods = findAllSubscribeMethods(subscriber.getClass());

        // 2. 线程安全地操作 eventMap
        synchronized (eventMap) {
            CopyOnWriteArraySet<Subscription> subscriptions = eventMap.get(eventId);
            if (subscriptions == null) {
                subscriptions = new CopyOnWriteArraySet<>();
                eventMap.put(eventId, subscriptions);
            }

            // 3. 遍历找到的方法并注册
            for (Method method : allMethods) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                if (annotation == null) continue;

                // 参数校验
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new LcaiHttpException("Method " + method.getName() + " must have exactly one parameter");
                }
                if (parameterTypes[0] != String.class) {
                    throw new LcaiHttpException("Subscribe method param must be String type");
                }

                Subscription newSubscription = new Subscription(subscriber, method, annotation.mode());
                if (!subscriptions.contains(newSubscription)) {
                    subscriptions.add(newSubscription);
                }
            }
        }

        MessageEvent stickyEvent = stickyEventMap.get(eventId);
        if (stickyEvent != null) {
            post(stickyEvent);
        }
    }

    /**
     * 递归查找父类方法
     */
    private List<Method> findAllSubscribeMethods(Class<?> clazz) {
        List<Method> methodList = new ArrayList<>();
        Class<?> current = clazz;

        // 向上遍历直到 Object
        while (current != null && current != Object.class) {
            Method[] declaredMethods = current.getDeclaredMethods();
            for (Method m : declaredMethods) {
                // 过滤掉 static 方法，防止内存泄漏或调用错误
                if (!Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Subscribe.class)) {
                    methodList.add(m);
                }
            }
            current = current.getSuperclass();
        }
        return methodList;
    }

    /**
     * 取消订阅
     */
    public void unRegister(@NonNull Object subscriber) {
        if (subscriber == null) return;

        synchronized (eventMap) {
            Iterator<Map.Entry<String, CopyOnWriteArraySet<Subscription>>> iterator = eventMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CopyOnWriteArraySet<Subscription>> entry = iterator.next();
                CopyOnWriteArraySet<Subscription> subscriptions = entry.getValue();

                if (subscriptions != null) {
                    // 使用迭代器安全移除
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        subscriptions.removeIf(subscription -> subscription.subscriber == subscriber);
                    }
                }

                // 如果该事件ID下没有订阅者了，移除该Key以节省内存
                if (subscriptions == null || subscriptions.isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 发送普通事件
     */
    public void post(@NonNull MessageEvent event) {
        if (event == null || TextUtils.isEmpty(event.msgId)) {
            throw new IllegalArgumentException("event and msgId can not be empty");
        }

        CopyOnWriteArraySet<Subscription> targetSubscriptions;
        synchronized (eventMap) {
            targetSubscriptions = eventMap.get(event.msgId);
        }

        if (targetSubscriptions != null && !targetSubscriptions.isEmpty()) {
            for (Subscription subscription : targetSubscriptions) {
                if (subscription.threadMode == ThreadMode.POSTING) {
                    invokeMethod(subscription.subscriber, subscription.subscribeMethod, event.msgContent);
                } else if (subscription.threadMode == ThreadMode.MAIN) {
                    invokeMainMethod(subscription.subscriber, subscription.subscribeMethod, event.msgContent);
                }
            }
        }
    }

    /**
     * 发送粘性事件
     */
    public void postSticky(@NonNull MessageEvent event) {
        if (event == null || TextUtils.isEmpty(event.msgId)) return;
        stickyEventMap.put(event.msgId, event);
        post(event);
    }

    public void removeStickyEvent(@NonNull String eventId) {
        stickyEventMap.remove(eventId);
    }

    private void invokeMethod(@NonNull Object subscriber, @NonNull Method method, @Nullable String msgContent) {
        try {
            method.setAccessible(true);
            method.invoke(subscriber, msgContent);
        } catch (InvocationTargetException e) {
            LcaiLogUtils.e("Event dispatch failed in method: " + method.getName());
            e.getCause().printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void invokeMainMethod(@NonNull Object subscriber, @NonNull Method method, @Nullable String msgContent) {
        mainHandler.post(() -> invokeMethod(subscriber, method, msgContent));
    }

    public void clearAll() {
        synchronized (eventMap) {
            eventMap.clear();
        }
        stickyEventMap.clear();
        mainHandler.removeCallbacksAndMessages(null);
        instance = null;
    }
}