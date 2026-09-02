package com.liucai.core.util.log;

import android.util.Log;

import com.liucai.core.apputils.GlobalAppUtil;
import com.liucai.core.apputils.GlobalModleString;
import com.liucai.http.thread.GlobalThreadPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LcaiLogUtils {
    private static final String TAG = "LcaiLogUtils";
    private static final int MAX_LOG_LENGTH = 1500;
    /**
     * 类名
     */
    public static String className;

    /**
     * 方法名
     */
    public static String methodName;

    /**
     * 所在行数
     */
    public static int lineNumber;

    public static void getClassNames(StackTraceElement[] elements) {
        className = elements[1].getClassName();
        methodName = elements[1].getMethodName();
        lineNumber = elements[1].getLineNumber();
    }

    public static void i(Object... objects) {
        if (isDebug()) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.INFO, className, fullLog);
        }
    }

    public static void d(Object... objects) {
        if (isDebug()) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.DEBUG, className, fullLog);
        }
    }

    public static void e(Object... objects) {
        if (isDebug()) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.ERROR, className, fullLog);
        }
    }

    public static void w(Object... objects) {
        if (isDebug()) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.WARN, className, fullLog);
        }
    }

    private static boolean isDebug() {
        boolean debug = (boolean) GlobalAppUtil.globalGetObject(GlobalModleString.GLOBAL_DEBUG_MODE, false);
        return debug;
    }

    /**
     * 构建完整的日志字符串
     */
    private static String buildLogString(Object... msg) {
        StringBuilder builder = new StringBuilder();
        builder.append(TAG).append("\nclassName:");
        builder.append(className);
        builder.append("\nmethodName:").append(methodName)
                .append("\nlineNumber:").append(lineNumber);

        for (int i = 0; i < msg.length; i++) {
            builder.append("\nparams[").append(i).append("]:");
            builder.append(msg[i]);
        }
        return builder.toString();
    }

    /**
     * 分段打印日志
     */
    private static void splitAndLog(int priority, String tag, String content) {
        // 如果日志长度小于等于最大限制，直接打印
        if (content.length() <= MAX_LOG_LENGTH) {
            Log.println(priority, tag, content);
            if (GlobalAppUtil.isSaveLog()) {
                writeLog(getLevel(priority),tag,content);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 分段打印
                for (int i = 0; i <= content.length() / MAX_LOG_LENGTH; i++) {
                    int start = i * MAX_LOG_LENGTH;
                    int end = Math.min((i + 1) * MAX_LOG_LENGTH, content.length());
                    String segment = content.substring(start, end);
                    String segmentTag = "LcaiLogUtils:" + "[" + (i + 1) + "/" +
                            ((content.length() + MAX_LOG_LENGTH - 1) / MAX_LOG_LENGTH) + "]";
                    Log.println(priority, segmentTag, segment);
                    if (GlobalAppUtil.isSaveLog()) {
                        writeLog(getLevel(priority),tag,content);
                    }
                }
            }
        }).start();
    }

    public static void writeLog(String level, String tag, String msg) {
        GlobalThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (GlobalAppUtil.getCacheFile() != null) {
                    File logDir = new File(GlobalAppUtil.getCacheFile(), "logs");
                    if (!logDir.exists()) {
                        logDir.mkdirs();
                    }
                    File logFile = new File(logDir, "app_log.txt");
                    if (logFile.exists() && logFile.length() > 5 * 1024 * 1024) {
                        logFile.delete();
                    }

                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
                    String logContent = String.format("%s [%s] %s: %s\n", time, level, tag, msg);

                    FileWriter writer = null;
                    try {
                        // true 表示追加模式
                        writer = new FileWriter(logFile, true);
                        writer.write(logContent);
                        writer.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "写入日志失败", e);
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }

    private static String getLevel(int level) {
        switch (level) {
            case 2:
                return "VERBOSE";
            case 3:
                return "DEBUG";
            case 4:
                return "INFO";
            case 5:
                return "WARN";
            case 6:
                return "ERROR";
            case 7:
                return "ASSERT";
            default:
                return "DEBUG";
        }
    }
}
