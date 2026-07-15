package com.liucai.eventBus;

import java.lang.reflect.Method;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/15
 */
class Subscription {
    final Object subscriber;
    final Method subscribeMethod;
    final ThreadMode threadMode;

    Subscription(Object subscriber, Method subscribeMethod, ThreadMode threadMode) {
        this.subscriber = subscriber;
        this.subscribeMethod = subscribeMethod;
        this.threadMode = threadMode;
    }

    // 基于订阅者实例和方法做相等判断，避免重复注册
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return subscriber == that.subscriber && subscribeMethod.equals(that.subscribeMethod);
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(subscriber);
        result = 31 * result + subscribeMethod.hashCode();
        return result;
    }
}
