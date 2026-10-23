package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * CREATE TABLE 语句生成器。
 */
public class Create<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Create(Class<?> aClass) {
        super(aClass);
    }

    public String createTable() {
        String tableName = getTableName(entity.getClass());
        String idColumn = CamelCaseConverter.toUnderscore(getTableId(entity.getClass()));

        Field[] fields = new DBUtils<>().getFields(entity.getClass());
        StringBuilder builder = new StringBuilder();
        int validCount = 0;

        for (Field field : fields) {
            // 过滤静态字段和瞬态字段
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            Class<?> type = field.getType();
            String column = CamelCaseConverter.toUnderscore(field.getName());

            builder.append(column).append(SPACE).append(mapSqlType(type));

            if (column.equals(idColumn)) {
                builder.append(SPACE).append(PRIMARY_KEY);
                if (type.equals(Integer.class) || type.equals(Long.class)
                        || type.equals(int.class) || type.equals(long.class)) {
                    builder.append(SPACE).append(AUTOINCREMENT);
                }
            }

            builder.append(",");
            validCount++;
        }

        // 移除最后一个多余的逗号
        if (validCount > 0) {
            builder.setLength(builder.length() - 1);
        }

        return format(TABLE_CREATE, tableName, builder.toString());
    }

    private String mapSqlType(Class<?> type) {
        if (type.equals(String.class)) {
            return TEXT;
        }
        if (type.equals(Integer.class) || type.equals(Long.class)
                || type.equals(int.class) || type.equals(long.class)
                || type.equals(Short.class) || type.equals(short.class)
                || type.equals(Boolean.class) || type.equals(boolean.class)) { // 修复: Boolean 映射为 INTEGER
            return INTEGER;
        }
        if (type.equals(Double.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(float.class)) {
            return REAL;
        }
        if (type.equals(Byte[].class) || type.equals(byte[].class)) {
            return BLOB;
        }
        return TEXT;
    }
}