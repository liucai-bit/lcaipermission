package com.liucai.eventBus;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * @author liucai
 * @description 订阅关系。subscriber 使用 WeakReference 持有，防止内存泄漏。
 * 当外部忘记调用 unRegister 时，GC 仍能回收 subscriber。
 */
class Subscription {
    // 关键改动：使用 WeakReference 包裹 subscriber
    final WeakReference<Object> subscriberRef;
    final Method subscribeMethod;
    final ThreadMode threadMode;

    Subscription(Object subscriber, Method subscribeMethod, ThreadMode threadMode) {
        this.subscriberRef = new WeakReference<>(subscriber);
        this.subscribeMethod = subscribeMethod;
        this.threadMode = threadMode;
    }

    /** 获取订阅者，可能返回 null（已被 GC）*/
    Object getSubscriber() {
        return subscriberRef.get();
    }

    /** 是否已失效（订阅者被 GC）*/
    boolean isSubscriberGone() {
        return subscriberRef.get() == null;
    }

    // 基于 subscriber 的引用和 method 做相等判断，避免重复注册
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        Object self = this.subscriberRef.get();
        Object other = that.subscriberRef.get();
        // 两个引用都为 null 时不认为相等（避免失效对象互相覆盖）
        if (self == null || other == null) {
            return false;
        }
        return self == other && subscribeMethod.equals(that.subscribeMethod);
    }

    @Override
    public int hashCode() {
        Object self = subscriberRef.get();
        // 若已失效，使用 Method 的 hashCode 作为兜底，避免 NPE
        int result = self != null ? System.identityHashCode(self) : 0;
        result = 31 * result + subscribeMethod.hashCode();
        return result;
    }
}