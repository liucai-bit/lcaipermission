package com.liucai.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

/**
 * 全局未捕获异常处理器。
 *
 * <p>应在 {@code Application#onCreate()} 中注册：
 * <pre>
 * Thread.setDefaultUncaughtExceptionHandler(LcaiException.getInstance());
 * </pre>
 *
 * @author liucai
 */
public final class LcaiUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final LcaiUncaughtExceptionHandler INSTANCE = new LcaiUncaughtExceptionHandler();

    /** 系统默认处理器，处理完自身逻辑后可委托给它。 */
    @Nullable
    private Thread.UncaughtExceptionHandler defaultHandler;

    private LcaiUncaughtExceptionHandler() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static LcaiUncaughtExceptionHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            logCrash(t, e);
        } catch (Throwable loggingError) {
            // 绝不能让日志逻辑把异常处理器本身搞崩
            loggingError.printStackTrace();
        }

        // 交给系统默认处理器，保证系统能正常记录并结束进程
        if (defaultHandler != null && defaultHandler != this) {
            defaultHandler.uncaughtException(t, e);
        }
    }

    private void logCrash(@NonNull Thread thread, @NonNull Throwable e) {
        StackTraceElement top = getTopFrame(e);

        String className = top != null ? top.getClassName() : "unknown";
        String methodName = top != null ? top.getMethodName() : "unknown";
        int lineNumber = top != null ? top.getLineNumber() : -1;

        // 打印完整堆栈（关键：不要只打一帧）
        LcaiLogUtils.e(
                "LcaiException Catch Exception",
                "线程: " + thread.getName(),
                "类型: " + e.getClass().getName(),
                "信息: " + safeMessage(e),
                "位置: " + className + "#" + methodName + ":" + lineNumber,
                "堆栈: " + stackTraceToString(e)
        );
    }

    /**
     * 安全地取最顶上的一帧（而不是固定 [1]，避免越界）。
     */
    @Nullable
    private StackTraceElement getTopFrame(@NonNull Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        if (stack == null || stack.length == 0) {
            return null;
        }
        // 取第一帧（异常真正抛出的位置），而不是 [1]
        return stack[0];
    }

    @NonNull
    private String safeMessage(@NonNull Throwable e) {
        String msg = e.getMessage();
        return msg != null ? msg : "(no message)";
    }

    @NonNull
    private String stackTraceToString(@NonNull Throwable e) {
        StringBuilder sb = new StringBuilder();
        // 包含 cause 链
        sb.append(Log.getStackTraceString(e));
        return sb.toString();
    }

    // 避免直接依赖 android.util.Log 的 import，这里用简单封装
    private static final class Log {
        static String getStackTraceString(Throwable t) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }
}