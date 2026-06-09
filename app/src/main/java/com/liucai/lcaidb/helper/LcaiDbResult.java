package com.liucai.lcaidb.helper;

public class LcaiDbResult {

    /**
     * 判断数据库是否操作成功
     * @param result
     * @return
     */
    public static boolean retBool(long result) {
        return result >= 1;
    }

}
