package com.liucai.core.util.log;

import android.util.Log;

import com.liucai.permission.BuildConfig;

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
        if (BuildConfig.DEBUG) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.INFO, className, fullLog);
        }

    }

    public static void d(Object... objects) {
        if (BuildConfig.DEBUG) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.DEBUG, className, fullLog);
        }
    }

    public static void e(Object... objects) {
        if (BuildConfig.DEBUG) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.ERROR, className, fullLog);
        }
    }

    public static void w(Object... objects) {
        if (BuildConfig.DEBUG) {
            getClassNames(new Throwable().getStackTrace());
            String fullLog = buildLogString(objects);
            splitAndLog(Log.WARN, className, fullLog);
        }
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
                }
            }
        }).start();
    }
}
