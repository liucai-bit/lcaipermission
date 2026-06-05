package com.liucai.core.util.log;

import android.util.Log;

public class LcaiLogUtils {
    private static final String TAG = "LcaiLogUtils";
    private static final int MAX_LOG_LENGTH = 4000;
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

    public static String Log(Object... msg) {
        StringBuilder builder = new StringBuilder();
        builder.append(TAG).append("\nclassName:");
        builder.append(className);
        builder.append("\nmethodName:").append(methodName).append("\nlineNumber:").append(lineNumber);

        for (Object obj : msg) {
            builder.append("\nparams:").append(obj);
        }
        return builder.toString();
    }

    public static void i(Object... objects) {
        getClassNames(new Throwable().getStackTrace());
        String fullLog = buildLogString(objects);
        splitAndLog(Log.INFO, className, fullLog);
    }

    public static void d(Object... objects) {
        getClassNames(new Throwable().getStackTrace());
        String fullLog = buildLogString(objects);
        splitAndLog(Log.DEBUG, className, fullLog);
    }

    public static void e(Object... objects) {
        getClassNames(new Throwable().getStackTrace());
        String fullLog = buildLogString(objects);
        splitAndLog(Log.ERROR, className, fullLog);
    }

    public static void w(Object... objects) {
        getClassNames(new Throwable().getStackTrace());
        String fullLog = buildLogString(objects);
        splitAndLog(Log.WARN, className, fullLog);
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

            // 处理超长字符串
            String paramStr = String.valueOf(msg[i]);
            if (paramStr.length() > 1000) { // 单个参数超过1000字符时截断显示
                builder.append(paramStr, 0, 1000)
                        .append("... [长度:").append(paramStr.length()).append("]");
            } else {
                builder.append(paramStr);
            }
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

        // 分段打印
        for (int i = 0; i <= content.length() / MAX_LOG_LENGTH; i++) {
            int start = i * MAX_LOG_LENGTH;
            int end = Math.min((i + 1) * MAX_LOG_LENGTH, content.length());

            String segment = content.substring(start, end);
            String segmentTag = tag + "[" + (i + 1) + "/" +
                    ((content.length() + MAX_LOG_LENGTH - 1) / MAX_LOG_LENGTH) + "]";

            Log.println(priority, segmentTag, segment);
        }
    }
}
