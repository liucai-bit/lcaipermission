package com.liucai.core.util.text;

import java.util.List;

/**
 * @author liucai
 * @program lcpermission
 * @description 字符串操作
 * @Date 2024-05-11 11:05
 **/
public class TextUtils<T>{

    /**
     * 字符串非空判断
     * @param str 等待判断字符串
     * @return
     */
    public static boolean isEmpty(String str){
        int strLen;
        if (str == null || (strLen = str.length()) == 0 || str.equals("null")) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }


    public static boolean equals(String arg, String... with) {
        if (TextUtils.isEmpty(arg)) {
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
        if (TextUtils.isEmpty(arg)) {
            return 0;
        }
        return arg.indexOf(with);
    }

    public static boolean haveOne(List<String> tList,String... entity) {
        for (String arg : tList) {
            if (TextUtils.equals(arg, entity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean haveOneArray(String[] entitys, String ... entity) {
        for (String arg : entitys) {
            if (TextUtils.equals(arg, entity)) {
                return true;
            }
        }
        return false;
    }
}
