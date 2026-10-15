package com.liucai.lcaidb.enu;

/**
 * SQL 排序方向。
 *
 * @author liucai
 */
public enum OrderTypeEnum {

    ASC("ASC"),
    DESC("DESC");

    private final String value;

    OrderTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}