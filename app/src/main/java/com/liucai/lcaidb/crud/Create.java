package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.lang.reflect.Field;

/**
 * CREATE TABLE 语句生成器。
 *
 * @author liucai
 */
public class Create<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Create(Class<?> aClass) {
        super(aClass);
    }

    public String createTable() {
        String tableName = getTableName(entity.getClass());
        // 注解值统一转为下划线形式后比较，避免注解写 "id" 而字段是 "userId" 等匹配问题
        String idColumn = CamelCaseConverter.toUnderscore(getTableId(entity.getClass()));

        Field[] fields = new DBUtils<>().getFields(entity.getClass());
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class<?> type = field.getType();
            String column = CamelCaseConverter.toUnderscore(field.getName());

            builder.append(column).append(SPACE).append(mapSqlType(type));

            if (column.equals(idColumn)) {
                builder.append(SPACE).append(PRIMARY_KEY);
                // AUTOINCREMENT 仅对 INTEGER PRIMARY KEY 有效
                if (type.equals(Integer.class) || type.equals(Long.class)
                        || type.equals(int.class) || type.equals(long.class)) {
                    builder.append(SPACE).append(AUTOINCREMENT);
                }
            }

            if (i < fields.length - 1) {
                builder.append(",");
            }
        }

        return format(TABLE_CREATE, tableName, builder.toString());
    }

    private String mapSqlType(Class<?> type) {
        if (type.equals(String.class)) {
            return TEXT;
        }
        if (type.equals(Integer.class) || type.equals(Long.class)
                || type.equals(int.class) || type.equals(long.class)
                || type.equals(Short.class) || type.equals(short.class)) {
            return INTEGER;
        }
        if (type.equals(Double.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(float.class)) {
            return REAL;
        }
        if (type.equals(Byte[].class) || type.equals(byte[].class)) {
            return BLOB;
        }
        // 未知类型默认 TEXT，避免 CREATE TABLE 缺列类型
        return TEXT;
    }
}