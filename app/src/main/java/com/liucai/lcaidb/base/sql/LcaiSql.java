package com.liucai.lcaidb.base.sql;

/**
 * SQL 语句模板与关键字常量。
 *
 * @author liucai
 */
public interface LcaiSql {
    String TABLE_CREATE = "CREATE TABLE %s (%s)";
    String TABLE_QUERY = "SELECT * FROM %s WHERE %s";
    String QUERYLIST = "SELECT * FROM %s";
    String DELETE_BYID = "DELETE FROM %s WHERE %s IN (%s)";
    String SELECT_COUNTS = "SELECT COUNT(*) AS COUNT FROM %s";
    String DELETE_BY_PARAMS = "DELETE FROM %s WHERE %s";
    String UPDATE_BY_ID = "UPDATE %s SET %s WHERE %s=%s";
    String UPDATE_BY_PARAMS = "UPDATE %s SET %s WHERE %s";
    String LIMIT_FOR = "LIMIT %s,%s";
    String ORDER_BY_FOR = "ORDER BY %s %s";
    String TABLE_EXIST = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'";

    /** 字符串字面量占位。SQLite 中必须用单引号，双引号是标识符。 */
    String STRING = "'%s'";

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

    /**
     * 格式化 SQL 模板。
     */
    default String format(String template, Object... args) {
        return String.format(template, args);
    }
}