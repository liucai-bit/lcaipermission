package com.liucai.eventBus;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description 轻量型基于事件ID的事件总线，修复多实例订阅冲突、反射调用异常等问题
 * @Date 2026/7/15
 */
public class LcaiEventBus {

    private static volatile LcaiEventBus instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 核心注册表结构：Key为事件ID，Value为该事件下所有订阅者的集合
    private final Map<String, CopyOnWriteArraySet<Subscription>> eventMap = new ConcurrentHashMap<>();

    // 粘性事件缓存，存储最近一次发送的粘性事件对象
    private final Map<String, MessageEvent> stickyEventMap = new ConcurrentHashMap<>();

    // 私有构造，禁止外部直接实例化
    private LcaiEventBus() {}

    /**
     * 双重校验锁获取单例，线程安全
     */
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
     * 注册订阅，新增重复检测、参数校验，支持同一个事件ID下多个订阅者共存
     * @param eventId 要订阅的事件唯一ID
     * @param subscriber 订阅者的实例对象，不能为null
     */
    public void register(@NonNull String eventId, @NonNull Object subscriber) {
        if (TextUtils.isEmpty(eventId) || subscriber == null) {
            throw new IllegalArgumentException("eventId and subscriber can not be empty");
        }

        Class<?> subClass = subscriber.getClass();
        Method[] methods = subClass.getDeclaredMethods();

        for (Method method : methods) {
            if (!method.isAnnotationPresent(Subscribe.class)) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new IllegalArgumentException("Method " + method.getName() + " must have exactly one parameter");
            }

            Class<?> parameterType = parameterTypes[0];
            // 校验参数类型必须是String，和事件发送格式对齐
            if (parameterType !=String.class) {
                throw new IllegalArgumentException("Subscribe method param must be String type");
            }

            Subscribe annotation = method.getAnnotation(Subscribe.class);
            Subscription newSubscription = new Subscription(subscriber, method, annotation.mode());

            // 获取事件对应的订阅集合，不存在则自动创建
            CopyOnWriteArraySet<Subscription> subscriptions = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                subscriptions = eventMap.computeIfAbsent(eventId, k -> new CopyOnWriteArraySet<>());
            }else {
                subscriptions = new CopyOnWriteArraySet<>();
                eventMap.put(eventId, subscriptions);
            }

            // 避免重复注册
            if (!subscriptions.contains(newSubscription)) {
                subscriptions.add(newSubscription);
            }
        }

        // 注册完成后，如果存在该事件的粘性事件，直接触发回调
        MessageEvent stickyEvent = stickyEventMap.get(eventId);
        if (stickyEvent != null) {
            post(stickyEvent);
        }
    }

    /**
     * 取消订阅，基于实例对象匹配，避免同类型多个实例互相干扰
     * @param subscriber 要取消的订阅者实例
     */
    public void unRegister(@NonNull Object subscriber) {
        if (subscriber == null) return;

        for (Map.Entry<String, CopyOnWriteArraySet<Subscription>> entry : eventMap.entrySet()) {
            CopyOnWriteArraySet<Subscription> subscriptions = entry.getValue();
            if (subscriptions == null || subscriptions.isEmpty()) {
                continue;
            }

            // 移除该实例对应的所有订阅
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                subscriptions.removeIf(subscription -> subscription.subscriber == subscriber);
            }else {
                if (subscriptions != null && !subscriptions.isEmpty()) {
                    Iterator<Subscription> iterator = subscriptions.iterator();
                    while (iterator.hasNext()) {
                        Subscription subscription = iterator.next();
                        // 使用 == 比较实例引用，确保精准移除特定对象
                        if (subscription.subscriber == subscriber) {
                            iterator.remove();
                        }
                    }
                }
            }

            // 订阅集合为空则清理对应事件ID，避免内存残留空对象
            if (subscriptions.isEmpty()) {
                eventMap.remove(entry.getKey());
            }
        }
    }

    /**
     * 发送普通事件，空安全校验，遍历所有匹配该事件ID的订阅回调
     * @param event 要发送的事件对象，不能为null
     */
    public void post(@NonNull MessageEvent event) {
        if (event == null || TextUtils.isEmpty(event.msgId)) {
            throw new IllegalArgumentException("event and msgId can not be empty");
        }

        CopyOnWriteArraySet<Subscription> targetSubscriptions = eventMap.get(event.msgId);
        if (targetSubscriptions == null || targetSubscriptions.isEmpty()) {
            return;
        }

        for (Subscription subscription : targetSubscriptions) {
            ThreadMode mode = subscription.threadMode;
            if (mode == ThreadMode.POSTING) {
                invokeMethod(subscription.subscriber, subscription.subscribeMethod, event.msgContent);
            } else if (mode == ThreadMode.MAIN) {
                invokeMainMethod(subscription.subscriber, subscription.subscribeMethod, event.msgContent);
            }
        }
    }

    /**
     * 发送粘性事件，会缓存事件对象，后续注册的订阅者也能立刻收到该事件
     * @param event 要发送的粘性事件对象
     */
    public void postSticky(@NonNull MessageEvent event) {
        if (event == null || TextUtils.isEmpty(event.msgId)) return;
        stickyEventMap.put(event.msgId, event);
        post(event);
    }

    /**
     * 移除指定事件ID对应的粘性事件缓存
     * @param eventId 要清理的粘性事件ID
     */
    public void removeStickyEvent(@NonNull String eventId) {
        stickyEventMap.remove(eventId);
    }

    /**
     * 当前线程直接执行反射回调，修复原代码传Class对象的致命错误，传入真实订阅实例
     */
    private void invokeMethod(@NonNull Object subscriber, @NonNull Method method, @Nullable String msgContent) {
        try {
            method.setAccessible(true);
            method.invoke(subscriber, msgContent);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Event dispatch failed, inner method error", e.getCause());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can not access subscribe method", e);
        }
    }

    /**
     * 切到主线程执行反射回调，修复原代码传参错误
     */
    private void invokeMainMethod(@NonNull Object subscriber, @NonNull Method method, @Nullable String msgContent) {
        mainHandler.post(() -> {
            try {
                method.setAccessible(true);
                method.invoke(subscriber, msgContent);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Main thread event dispatch failed", e.getCause());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can not access subscribe method in main thread", e);
            }
        });
    }

    /**
     * 清空所有资源，在Application退出时调用，避免内存泄漏
     */
    public void clearAll() {
        eventMap.clear();
        stickyEventMap.clear();
        mainHandler.removeCallbacksAndMessages(null);
        instance = null;
    }
}
