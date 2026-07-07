package com.liucai.core.exception;

import androidx.annotation.NonNull;

import com.liucai.core.util.log.LcaiLogUtils;


/**
 * @author liucai
 * @program lctipsdialog
 * @description
 * @Date 2026/6/1
 */
public class LcaiException implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String className = e.getStackTrace()[1].getClassName();
        String methodName = e.getStackTrace()[1].getMethodName();
        int lineNumber = e.getStackTrace()[1].getLineNumber();

        LcaiLogUtils.e("LcaiException Catch Exception",
                "捕捉到异常",
                "ClassName:"+className,
                "MethodName:"+methodName,
                "LineNumber:"+lineNumber,
                "Exception:"+e.getMessage());
    }
}
