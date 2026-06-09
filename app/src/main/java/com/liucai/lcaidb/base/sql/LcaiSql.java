package com.liucai.lcaidb.base.sql;

/**
 * @author liucai
 * @program lcpermission
 * @description sql语句常量
 * @Date 2026/6/8
 */
public interface LcaiSql {

    /**
     * 创建表语句
     */
    String TABLE_CREATE = "CREATE TABLE %s (%s)";

    /**
     * 查询表语句
     * 条件查询
     */
    String TABLE_QUERY = "SELECT * FROM %s WHERE %s";

    /**
     * 查询语句
     * 查询全部数据
     */
    String QUERYLIST = "SELECT * FROM %s";

    /**
     * 条件查询
     * IN 语句
     */
    String DELETE_BYID = "DELETE FROM %s WHERE %s IN (%s)";

    /**
     * 查询表数据总条数
     */
    String SELECT_COUNTS = "SELECT COUNT(*) AS COUNT FROM %s";

    /**
     * 删除语句
     * 条件删除
     */
    String DELETE_BY_PARAMS = "DELETE FROM %s WHERE %s";

    /**
     * 更新语句
     * 条件更新
     */
    String UPDATE_BY_ID = "UPDATE %s set %s WHERE %s=%s";

    /**
     * 更新语句
     * WHERE 语句手动
     */
    String UPDATE_BY_PARAMS = "UPDATE %s set %s WHERE %s";

    /**
     * 分页查询
     */
    String LIMIT_FOR = "LIMIT %s,%s";

    /**
     * 排序语句
     */
    String ORDER_BY_FOR = "ORDER BY %s %s";

    String STRING = "\"%s\"";

    String PRIMARY_KEY = "PRIMARY KEY";

    String AUTOINCREMENT = "AUTOINCREMENT";

    String WHERE = "WHERE";

    String COUNT = "COUNT";

    String AND = "AND";

    String ASC = "ASC";

    String DESC = "DESC";

    String LIMIT = "LIMIT";

    String SPACE = " ";

    String INTEGER = "INTEGER";

    String REAL = "REAL";

    String TEXT = "TEXT";

    String BLOB = "BLOB";

    String TABLE_EXIST = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'";

    default String format(String arg, Object ... args) {
        return String.format(arg, args);
    }

}
