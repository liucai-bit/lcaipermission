package com.liucai.lcaidb.helper;

/**
 * 数据库执行结果判断工具。
 *
 * @author liucai
 */
public final class LcaiDbResult {

    private LcaiDbResult() {
    }

    /**
     * insert/update/delete 返回的行数或 rowId 是否代表成功。
     * SQLite 返回 -1 表示失败，>= 1 表示成功（0 也视为失败，因为没有行受影响）。
     */
    public static boolean retBool(long result) {
        return result >= 1;
    }
}