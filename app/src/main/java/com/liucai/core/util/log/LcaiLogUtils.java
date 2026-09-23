package com.liucai.core.util.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.apputils.GlobalModelString;
import com.liucai.http.thread.GlobalThreadPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志工具。
 *
 * <p>开关由 {@code GlobalModelString.GLOBAL_DEBUG_MODE} 控制，
 * 关闭时所有方法几乎零开销（先判断再构建字符串）。
 *
 * @author liucai
 */
public final class LcaiLogUtils {

    private static final String TAG = "LcaiLogUtils";

    /** Android Log 单行长度限制（约 4KB），这里保守取 3KB。 */
    private static final int MAX_LOG_LENGTH = 3000;

    /** 单个日志文件大小上限（5MB）。 */
    private static final long MAX_LOG_FILE_SIZE = 5L * 1024 * 1024;

    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILE_NAME = "app_log.txt";

    /** SimpleDateFormat 不是线程安全的，用 ThreadLocal 复用。 */
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() ->
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()));

    private LcaiLogUtils() {
    }

    // ---------------- 日志级别入口 ----------------

    public static void i(Object... msg) {
        log(Log.INFO, msg);
    }

    public static void d(Object... msg) {
        log(Log.DEBUG, msg);
    }

    public static void e(Object... msg) {
        log(Log.ERROR, msg);
    }

    public static void w(Object... msg) {
        log(Log.WARN, msg);
    }

    /**
     * 打印异常 + 完整堆栈。
     */
    public static void e(@NonNull String message, @NonNull Throwable t) {
        if (!isDebug()) {
            return;
        }
        StackTraceElement caller = findCaller();
        String full = buildLogString(caller, message, Log.getStackTraceString(t));
        writeAndLog(Log.ERROR, caller, full, true);
    }

    /**
     * 打印异常 + 完整堆栈（无消息）。
     */
    public static void e(@NonNull Throwable t) {
        e("", t);
    }

    // ---------------- 核心实现 ----------------

    private static void log(int priority, @Nullable Object... msg) {
        // ① 先判断开关，debug 关闭时直接返回，避免构建字符串的开销
        if (!isDebug()) {
            return;
        }
        // ② 取调用方信息（修复静态字段并发问题）
        StackTraceElement caller = findCaller();
        if (caller == null) {
            return;
        }
        String content = buildLogString(caller, msg);
        writeAndLog(priority, caller, content, false);
    }

    /**
     * 找到真正调用日志的类（跳过 LcaiLogUtils 自身）。
     *
     * <p>用局部变量保存，不再写静态字段，避免多线程覆盖。
     */
    @Nullable
    private static StackTraceElement findCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        // 0 = findCaller
        // 1 = log / e(String, Throwable)
        // 2 = 调用方
        // 为稳妥起见，跳过所有 LcaiLogUtils 自身的帧
        for (int i = 2; i < stack.length; i++) {
            StackTraceElement e = stack[i];
            if (!LcaiLogUtils.class.getName().equals(e.getClassName())) {
                return e;
            }
        }
        return stack.length > 2 ? stack[2] : null;
    }

    // ---------------- 构建日志字符串 ----------------

    private static String buildLogString(@NonNull StackTraceElement caller, @Nullable Object... msg) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(TAG)
                .append("\nclassName:").append(caller.getClassName())
                .append("\nmethodName:").append(caller.getMethodName())
                .append("\nlineNumber:").append(caller.getLineNumber());
        if (msg != null) {
            for (int i = 0; i < msg.length; i++) {
                sb.append("\nparams[").append(i).append("]:").append(msg[i]);
            }
        }
        return sb.toString();
    }

    private static String buildLogString(@NonNull StackTraceElement caller,
                                         @NonNull String message,
                                         @NonNull String stackTrace) {
        return TAG
                + "\nclassName:" + caller.getClassName()
                + "\nmethodName:" + caller.getMethodName()
                + "\nlineNumber:" + caller.getLineNumber()
                + "\nmessage:" + message
                + "\n" + stackTrace;
    }

    // ---------------- 输出（Logcat + 文件） ----------------

    private static void writeAndLog(int priority,
                                    @NonNull StackTraceElement caller,
                                    @NonNull String content,
                                    boolean isError) {
        // 1. 落盘（异步）
        if (GlobalAppUtil.isSaveLog()) {
            writeLogAsync(priority, caller, content);
        }

        // 2. Logcat（同步）
        if (content.length() <= MAX_LOG_LENGTH) {
            Log.println(priority, caller.getClassName(), content);
            return;
        }
        // 长日志分段 —— 直接在调用线程分段，不再新建线程
        // 注意：分段是为了规避 Logcat 单行长度限制，落盘只做一次，已经在上面的 writeLogAsync 完成
        int total = content.length();
        int segIndex = 1;
        int segCount = (total + MAX_LOG_LENGTH - 1) / MAX_LOG_LENGTH;
        for (int start = 0; start < total; start += MAX_LOG_LENGTH) {
            int end = Math.min(start + MAX_LOG_LENGTH, total);
            String tag = caller.getClassName() + "[" + segIndex + "/" + segCount + "]";
            Log.println(priority, tag, content.substring(start, end));
            segIndex++;
        }
    }

    // ---------------- 文件写入 ----------------

    private static void writeLogAsync(int priority,
                                      @NonNull StackTraceElement caller,
                                      @NonNull String content) {
        final String level = levelName(priority);
        final String tag = caller.getClassName();
        GlobalThreadPool.execute(() -> writeLogInternal(level, tag, content));
    }

    private static void writeLogInternal(@NonNull String level,
                                         @NonNull String tag,
                                         @NonNull String msg) {
        File cache = GlobalAppUtil.getCacheFile();
        if (cache == null) {
            return;
        }
        File logDir = new File(cache, LOG_DIR_NAME);
        if (!logDir.exists() && !logDir.mkdirs()) {
            return;
        }
        File logFile = new File(logDir, LOG_FILE_NAME);

        // 滚动：超过上限就重命名归档，避免直接删除
        if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
            File bak = new File(logDir, LOG_FILE_NAME + ".bak");
            if (bak.exists()) {
                //noinspection ResultOfMethodCallIgnored
                bak.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            logFile.renameTo(bak);
        }

        String time = DATE_FORMAT.get().format(new Date());
        String logContent = time + " [" + level + "] " + tag + ": " + msg + "\n";

        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(logContent);
            writer.flush();
        } catch (IOException e) {
            // 文件写入失败时，退回 Logcat，避免异常被吞
            Log.e(TAG, "write log failed", e);
        }
    }

    // ---------------- 工具 ----------------

    private static boolean isDebug() {
        // 注意：这里每次都会走 globalGetObject，性能敏感场景建议在 GlobalAppUtil 里缓存
        return GlobalAppUtil.isDebug();
    }

    @NonNull
    private static String levelName(int priority) {
        switch (priority) {
            case Log.VERBOSE: return "VERBOSE";
            case Log.DEBUG:   return "DEBUG";
            case Log.INFO:    return "INFO";
            case Log.WARN:    return "WARN";
            case Log.ERROR:   return "ERROR";
            case Log.ASSERT:  return "ASSERT";
            default:          return "DEBUG";
        }
    }
}