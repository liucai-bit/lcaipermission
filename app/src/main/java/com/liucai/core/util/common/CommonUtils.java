package com.liucai.core.util.common;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/7/30
 */
public class CommonUtils {

    /**
     * dp单位转px单位，自动适配当前设备屏幕密度
     * @param dpValue 输入dp数值
     * @param mContext 上下文对象
     * @return 转换后的像素值，非法输入默认返回0
     */
    public static int dip2px(Context mContext,float dpValue) {
        if (dpValue <= 0) return 0;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                mContext.getResources().getDisplayMetrics()
        );
    }

    /**
     * px单位转dp单位，适配不同屏幕密度下的数值还原
     * @param pxValue 输入像素数值
     * @param mContext 上下文对象
     * @return 转换后的dp数值，非法输入默认返回0
     */
    public static int px2dip(Context mContext,float pxValue) {
        if (pxValue <= 0) return 0;
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                pxValue,
                mContext.getResources().getDisplayMetrics()
        );
    }
}
