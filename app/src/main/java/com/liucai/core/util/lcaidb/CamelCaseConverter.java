package com.liucai.core.util.lcaidb;

public class CamelCaseConverter {
    /**
     * 将下划线转换成驼峰命名
     *
     * @param arg 字符串
     * @return
     */
    public static String underscoreToCamel(String arg) {

        if (!arg.contains("_")) {
            return toUpperCaseFirst(arg);
        }

        String args[] = arg.split("_");
        StringBuilder builder = new StringBuilder();
        for (String s : args) {
            builder.append(s.substring(0, 1).toUpperCase());
            builder.append(s.substring(1));
        }
        return builder.toString();
    }

    /**
     * 将首字母大写
     *
     * @param arg
     * @return
     */
    public static String toUpperCaseFirst(String arg) {
        String result = arg.substring(0, 1).toUpperCase();
        return result + arg.substring(1);
    }

    /**
     * 将驼峰转换成下划线
     * @param arg
     * @return
     */
    public static String toUnderscore(String arg) {
        StringBuilder builder = new StringBuilder();
        builder.append(arg.substring(0, 1).toLowerCase());
        for (int i = 1; i < arg.length(); i++) {
            String s = arg.substring(i, i + 1);
            if (s.equals(s.toUpperCase()) && !s.equals("_")) {
                builder.append("_");
                builder.append(s.toLowerCase());
            } else {
                builder.append(s);
            }
        }
        return builder.toString();
    }

}
