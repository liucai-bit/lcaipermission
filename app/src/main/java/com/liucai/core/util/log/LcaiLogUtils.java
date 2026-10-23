package com.liucai.core.util.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.apputils.GlobalAppUtil;
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
 * @author liucai
 */
public final class LcaiLogUtils {

    private static final String TAG = "LcaiLogUtils";

    private static final int MAX_LOG_LENGTH = 3000;

    private static final long MAX_LOG_FILE_SIZE = 5L * 1024 * 1024;

    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILE_NAME = "app_log.txt";

    private static final Object FILE_LOCK = new Object();

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()));

    private LcaiLogUtils() {
    }

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

    public static void e(@NonNull String message, @NonNull Throwable t) {
        if (!isDebug()) {
            return;
        }

        StackTraceElement caller = findCaller();
        if (caller == null) {
            return;
        }

        String full = buildLogString(
                caller, message, Log.getStackTraceString(t));
        writeAndLog(Log.ERROR, caller, full);
    }

    public static void e(@NonNull Throwable t) {
        e("", t);
    }

    private static void log(int priority, @Nullable Object... msg) {
        if (!isDebug()) {
            return;
        }

        StackTraceElement caller = findCaller();
        if (caller == null) {
            return;
        }

        String content = buildLogString(caller, msg);
        writeAndLog(priority, caller, content);
    }

    @Nullable
    private static StackTraceElement findCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();

        for (int i = 2; i < stack.length; i++) {
            StackTraceElement e = stack[i];
            if (!LcaiLogUtils.class.getName().equals(e.getClassName())) {
                return e;
            }
        }

        return stack.length > 2 ? stack[2] : null;
    }

    private static String buildLogString(@NonNull StackTraceElement caller,
                                         @Nullable Object... msg) {
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

    private static void writeAndLog(int priority,
                                    @NonNull StackTraceElement caller,
                                    @NonNull String content) {
        if (GlobalAppUtil.isSaveLog()) {
            writeLogAsync(priority, caller, content);
        }

        if (content.length() <= MAX_LOG_LENGTH) {
            Log.println(priority, caller.getClassName(), content);
            return;
        }

        int total = content.length();
        int segIndex = 1;
        int segCount = (total + MAX_LOG_LENGTH - 1) / MAX_LOG_LENGTH;

        for (int start = 0; start < total; start += MAX_LOG_LENGTH) {
            int end = Math.min(start + MAX_LOG_LENGTH, total);
            String tag = caller.getClassName()
                    + "[" + segIndex + "/" + segCount + "]";
            Log.println(priority, tag, content.substring(start, end));
            segIndex++;
        }
    }

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

        synchronized (FILE_LOCK) {
            if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
                File bak = new File(logDir, LOG_FILE_NAME + ".bak");
                if (bak.exists()) {
                    bak.delete();
                }
                logFile.renameTo(bak);
            }

            String time = DATE_FORMAT.get().format(new Date());
            String logContent = time + " [" + level + "] " + tag + ": " + msg + "\n";

            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(logContent);
                writer.flush();
            } catch (IOException e) {
                Log.e(TAG, "write log failed", e);
            }
        }
    }

    private static boolean isDebug() {
        return GlobalAppUtil.isDebug();
    }

    @NonNull
    private static String levelName(int priority) {
        switch (priority) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ASSERT";
            default:
                return "DEBUG";
        }
    }
}