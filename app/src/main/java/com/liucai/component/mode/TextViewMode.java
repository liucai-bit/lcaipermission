package com.liucai.component.mode;

import android.graphics.Typeface;

/**
 * @author HUAWEI
 * @program lcpermission
 * @description
 * @Date 2026/10/12
 */
public class TextViewMode {
    public enum TextFontStyle {
        DEFALUT,
        BLOD,
        ITALIC,
        BLOD_ITALIC
    }

    public static int getTypeface(TextFontStyle style) {
        switch (style) {
            case DEFALUT:
                return Typeface.NORMAL;
            case BLOD:
                return Typeface.BOLD;
            case ITALIC:
                return Typeface.ITALIC;
            case BLOD_ITALIC:
                return Typeface.BOLD_ITALIC;
            default:
                return Typeface.NORMAL;
        }
    }
}
