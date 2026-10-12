package com.liucai.core.util.common;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author liucai
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
     * px单位转dp单位
     * 注意：TypedValue.applyDimension 无法直接用于 px->dp，需手动除以 density
     */
    public static int px2dip(Context mContext, float pxValue) {
        if (pxValue <= 0) return 0;
        // 获取当前设备的屏幕密度系数
        float density = mContext.getResources().getDisplayMetrics().density;
        // 公式：dp = px / density
        // + 0.5f 是为了实现四舍五入，避免强制类型转换造成的精度丢失
        return (int) (pxValue / density + 0.5f);
    }
}
