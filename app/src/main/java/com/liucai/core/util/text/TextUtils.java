package com.liucai.core.util.text;

import java.util.List;

/**
 * @author liucai
 * @description 字符串操作
 */
public class TextUtils<T> {

    /**
     * 字符串非空判断。
     */
    public static boolean isEmpty(String str) {
        int strLen;
        if (str == null
                || (strLen = str.length()) == 0
                || "null".equals(str)) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean equals(String arg, String... with) {
        if (isEmpty(arg) || with == null) {
            return false;
        }

        for (String s : with) {
            if (arg.equals(s)) {
                return true;
            }
        }

        return false;
    }

    public static int indexOf(String arg, String with) {
        if (isEmpty(arg)) {
            return 0;
        }
        return arg.indexOf(with);
    }

    public static boolean haveOne(List<String> tList, String... entity) {
        if (tList == null || entity == null) {
            return false;
        }

        for (String arg : tList) {
            if (equals(arg, entity)) {
                return true;
            }
        }

        return false;
    }

    public static boolean haveOneArray(String[] entitys, String... entity) {
        if (entitys == null || entity == null) {
            return false;
        }

        for (String arg : entitys) {
            if (equals(arg, entity)) {
                return true;
            }
        }

        return false;
    }
}