package com.liucai.permission.util.log;

import android.util.Log;

public class LcaiLogUtils {
    private static final String TAG = "LcaiLogUtils";
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

        Log.i(className, Log(objects));
    }

    public static void d(Object... objects) {
        getClassNames(new Throwable().getStackTrace());

        Log.d(className, Log(objects));
    }

    public static void e(Object... objects) {
        getClassNames(new Throwable().getStackTrace());

        Log.e(className, Log(objects));
    }

    public static void w(Object... objects) {
        getClassNames(new Throwable().getStackTrace());

        Log.w(className, Log(objects));
    }
}
