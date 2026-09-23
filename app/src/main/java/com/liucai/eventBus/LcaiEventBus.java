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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author liucai
 * @description 轻量型基于事件ID的事件总线
 * 订阅者使用 WeakReference 持有，避免忘记 unRegister 导致的内存泄漏。
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

        while (current != null && current != Object.class) {
            Method[] declaredMethods = current.getDeclaredMethods();
            for (Method m : declaredMethods) {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // 关键改动：WeakReference 版本，需要用 getSubscriber() 判等
                        subscriptions.removeIf(subscription -> {
                            Object sub = subscription.getSubscriber();
                            return sub == null || sub == subscriber;
                        });
                    }
                }

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

        if (targetSubscriptions == null || targetSubscriptions.isEmpty()) {
            return;
        }

        // 遍历过程中收集已失效的订阅者，遍历结束后统一清理（避免 CopyOnWriteArraySet 迭代中修改）
        List<Subscription> deadSubscriptions = null;

        for (Subscription subscription : targetSubscriptions) {
            Object subscriber = subscription.getSubscriber();
            if (subscriber == null) {
                // 订阅者已被 GC，收集以便后续清理
                if (deadSubscriptions == null) {
                    deadSubscriptions = new ArrayList<>();
                }
                deadSubscriptions.add(subscription);
                continue;
            }

            if (subscription.threadMode == ThreadMode.POSTING) {
                invokeMethod(subscriber, subscription.subscribeMethod, event.msgContent);
            } else if (subscription.threadMode == ThreadMode.MAIN) {
                invokeMainMethod(subscriber, subscription.subscribeMethod, event.msgContent);
            }
        }

        // 统一清理失效的订阅者
        if (deadSubscriptions != null && !deadSubscriptions.isEmpty()) {
            synchronized (eventMap) {
                CopyOnWriteArraySet<Subscription> current = eventMap.get(event.msgId);
                if (current != null) {
                    current.removeAll(deadSubscriptions);
                    if (current.isEmpty()) {
                        eventMap.remove(event.msgId);
                    }
                }
            }
            LcaiLogUtils.d("LcaiEventBus: cleaned " + deadSubscriptions.size()
                    + " dead subscriptions for event: " + event.msgId);
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
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void invokeMainMethod(@NonNull Object subscriber, @NonNull Method method, @Nullable String msgContent) {
        mainHandler.post(() -> {
            // 二次检查：post 到主线程执行时，subscriber 可能已被 GC
            // 由于这里捕获了 subscriber 强引用（参数），只要进入方法，引用就是有效的
            invokeMethod(subscriber, method, msgContent);
        });
    }

    /**
     * 主动清理所有失效的订阅者（可选，也可由外部定期调用）
     */
    public void purgeDeadSubscriptions() {
        synchronized (eventMap) {
            Iterator<Map.Entry<String, CopyOnWriteArraySet<Subscription>>> iterator = eventMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CopyOnWriteArraySet<Subscription>> entry = iterator.next();
                CopyOnWriteArraySet<Subscription> subscriptions = entry.getValue();
                if (subscriptions != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        subscriptions.removeIf(Subscription::isSubscriberGone);
                    }
                }
                if (subscriptions == null || subscriptions.isEmpty()) {
                    iterator.remove();
                }
            }
        }
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