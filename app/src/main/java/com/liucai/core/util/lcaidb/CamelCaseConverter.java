package com.liucai.core.util.lcaidb;

/**
 * 命名转换工具。
 */
public class CamelCaseConverter {

    /**
     * 将下划线转换成小驼峰命名（如 user_name -> userName）。
     * <p>如果没有下划线，则原样返回，避免破坏正常的属性名。
     */
    public static String underscoreToCamel(String arg) {
        if (arg == null || arg.isEmpty()) {
            return "";
        }

        if (!arg.contains("_")) {
            // 修复：如果没有下划线，原样返回。原版转首字母大写会导致无法与实体属性名匹配
            return arg;
        }

        String[] args = arg.split("_");
        StringBuilder builder = new StringBuilder(arg.length());

        boolean first = true;
        for (String s : args) {
            if (s == null || s.isEmpty()) {
                continue;
            }
            if (first) {
                // 第一个单词首字母小写
                builder.append(Character.toLowerCase(s.charAt(0)));
                if (s.length() > 1) {
                    builder.append(s.substring(1));
                }
                first = false;
            } else {
                // 后续单词首字母大写
                builder.append(Character.toUpperCase(s.charAt(0)));
                if (s.length() > 1) {
                    builder.append(s.substring(1));
                }
            }
        }

        return builder.toString();
    }

    /**
     * 将首字母大写。
     */
    public static String toUpperCaseFirst(String arg) {
        if (arg == null || arg.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(arg.charAt(0)) + arg.substring(1);
    }

    /**
     * 将驼峰转换成下划线。
     * <p>支持连续大写（如 myURL -> my_url），且首字母大写（如 UserId -> user_id）安全。
     */
    public static String toUnderscore(String arg) {
        if (arg == null || arg.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(arg.length() + 4);

        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);

            // 判断是否为大写字母
            if (Character.isUpperCase(c)) {
                // 满足以下两种情况之一时，需要在前面加下划线：
                // 1. 前一个字符是小写字母（如 userName 的 N）
                // 2. 前一个字符是大写字母，但后一个字符是小写字母（如 URLValue 的 L，将其分为 url_value）
                boolean needUnderscore = false;
                if (i > 0) {
                    char prev = arg.charAt(i - 1);
                    if (Character.isLowerCase(prev)) {
                        needUnderscore = true;
                    } else if (Character.isUpperCase(prev) && i + 1 < arg.length() && Character.isLowerCase(arg.charAt(i + 1))) {
                        needUnderscore = true;
                    }
                }

                if (needUnderscore) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}