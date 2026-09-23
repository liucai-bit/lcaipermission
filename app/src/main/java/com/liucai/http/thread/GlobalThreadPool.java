package com.liucai.http.thread;

import androidx.annotation.NonNull;

import com.liucai.core.util.log.LcaiLogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局 HTTP 线程池。
 *
 * <p>线程数按 IO 密集型任务配置（HTTP 请求大部分时间在等 IO），
 * 与 CPU 核心数关系不大，因此核心/最大线程数直接固定，避免低端设备任务被饿死。
 *
 * @author liucai
 */
public final class GlobalThreadPool {

    private static final String TAG = "GlobalThreadPool";

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 16;
    private static final int KEEP_ALIVE_SECONDS = 30;
    private static final int QUEUE_CAPACITY = 128;

    private static volatile ThreadPoolExecutor sExecutor;

    private GlobalThreadPool() {
    }

    public static ThreadPoolExecutor getInstance() {
        if (sExecutor == null) {
            synchronized (GlobalThreadPool.class) {
                if (sExecutor == null || sExecutor.isShutdown()) {
                    sExecutor = createExecutor();
                }
            }
        }
        return sExecutor;
    }

    private static ThreadPoolExecutor createExecutor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ThreadFactory factory = new DefaultThreadFactory();

        // 队列满时不阻塞调用线程（可能是主线程），直接丢弃并记录日志
        RejectedExecutionHandler handler = (r, executor) ->
                LcaiLogUtils.e(TAG, "Task rejected: queue full, active=" + executor.getActiveCount());

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                queue,
                factory,
                handler);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public static void execute(@NonNull Runnable task) {
        try {
            getInstance().execute(task);
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "Execute task failed", e);
        }
    }

    public static Future<?> submit(@NonNull Runnable task) {
        try {
            return getInstance().submit(task);
        } catch (Exception e) {
            LcaiLogUtils.e(TAG, "Submit task failed", e);
            return null;
        }
    }

    public static boolean remove(@NonNull Runnable task) {
        ThreadPoolExecutor e = sExecutor;
        return e != null && e.getQueue().remove(task);
    }

    public static void shutdown() {
        ThreadPoolExecutor e = sExecutor;
        if (e != null && !e.isShutdown()) {
            e.shutdown();
        }
        sExecutor = null;
    }

    public static void shutdownNow() {
        ThreadPoolExecutor e = sExecutor;
        if (e != null && !e.isShutdown()) {
            e.shutdownNow();
        }
        sExecutor = null;
    }

    private static final class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String prefix;

        DefaultThreadFactory() {
            prefix = "LcaiHttp-" + POOL.getAndIncrement() + "-";
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r, prefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}