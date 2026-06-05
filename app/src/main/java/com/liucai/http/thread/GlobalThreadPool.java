package com.liucai.http.thread;

import android.os.Process;
import android.util.Log;

import com.liucai.core.util.log.LcaiLogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局线程池管理器
 * 用于统一管理 HTTP 请求及其他异步任务
 */
public class GlobalThreadPool {

    private static final String TAG = "GlobalThreadPool";

    // CPU 核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // 核心线程数：CPU核心数 + 1
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    // 最大线程数：CPU核心数 * 2 + 1
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    // 非核心线程空闲存活时间
    private static final int KEEP_ALIVE_SECONDS = 30;

    // 任务队列容量
    private static final int BLOCKING_QUEUE_CAPACITY = 128;

    private static volatile ThreadPoolExecutor sExecutor;
    private static final Object sLock = new Object();

    /**
     * 获取单例线程池实例
     * 双重检查锁定保证线程安全
     */
    public static ThreadPoolExecutor getInstance() {
        if (sExecutor == null) {
            synchronized (sLock) {
                if (sExecutor == null) {
                    // 创建阻塞队列
                    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);

                    // 创建线程工厂
                    ThreadFactory threadFactory = new DefaultThreadFactory();

                    // 创建拒绝策略：当队列满且线程达到最大值时，由调用线程执行任务（防止任务丢失）
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

                    sExecutor = new ThreadPoolExecutor(
                            CORE_POOL_SIZE,
                            MAXIMUM_POOL_SIZE,
                            KEEP_ALIVE_SECONDS,
                            TimeUnit.SECONDS,
                            workQueue,
                            threadFactory,
                            handler
                    );

                    // 允许核心线程超时回收，节省资源
                    sExecutor.allowCoreThreadTimeOut(true);

                    LcaiLogUtils.d(TAG, "GlobalThreadPool initialized. Core: " + CORE_POOL_SIZE + ", Max: " + MAXIMUM_POOL_SIZE);
                }
            }
        }
        return sExecutor;
    }

    /**
     * 执行 Runnable 任务
     * @param runnable 任务对象
     */
    public static void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        try {
            getInstance().execute(runnable);
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "Execute task failed", e);
        }
    }

    /**
     * 提交 Callable 或 Runnable 任务，返回 Future
     * 用于需要获取返回值或取消任务的场景
     * @param task 任务对象
     * @return Future 对象
     */
    public static java.util.concurrent.Future<?> submit(Runnable task) {
        if (task == null) {
            return null;
        }
        try {
            return getInstance().submit(task);
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "Submit task failed", e);
            return null;
        }
    }

    /**
     * 移除未执行的任务
     * @param task 任务对象
     * @return 是否移除成功
     */
    public static boolean remove(Runnable task) {
        if (task == null || sExecutor == null) {
            return false;
        }
        return sExecutor.getQueue().remove(task);
    }

    /**
     * 关闭线程池
     * 通常在应用退出时调用
     */
    public static void shutdown() {
        if (sExecutor != null && !sExecutor.isShutdown()) {
            sExecutor.shutdown();
            LcaiLogUtils.d(TAG, "GlobalThreadPool shutdown");
        }
    }

    /**
     * 立即关闭线程池
     * 尝试停止所有正在执行的任务
     */
    public static void shutdownNow() {
        if (sExecutor != null && !sExecutor.isShutdown()) {
            sExecutor.shutdownNow();
            LcaiLogUtils.d(TAG, "GlobalThreadPool shutdownNow");
        }
    }

    /**
     * 默认线程工厂
     * 设置线程名称优先级，便于调试和性能优化
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "LcaiHttp-Pool-" + poolNumber.getAndIncrement() + "-Thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

            // 设置线程优先级为后台线程，避免占用主线程资源
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            // 设置线程所属进程优先级（Android特有优化）
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            return t;
        }
    }
}
