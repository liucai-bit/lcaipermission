package com.liucai.lcaidb.crud;

import androidx.annotation.NonNull;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.lcaidb.LcaiAspect;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class Create<T extends LcaiTableInfo> extends BaseCRUD<T> {

    public Create(Class<?> aClass){
        super(aClass);
    }

    public String createTable() {
        String tableName = getTableName(entity.getClass());
        String tableId = getTableId(entity.getClass());
        Field[] fields = new DBUtils<>().getFields(entity.getClass());
        StringBuilder builder = new StringBuilder();
        int position = 0;
        for (Field field : fields) {
            Class<?> type = field.getType();
            builder.append(CamelCaseConverter.toUnderscore(field.getName())).append(SPACE);
            if (type.equals(String.class)) {
                builder.append(TEXT).append(SPACE);
            } else if (type.equals(Integer.class) || type.equals(Long.class)) {
                builder.append(INTEGER).append(SPACE);
            } else if (type.equals(Double.class) || type.equals(Float.class)) {
                builder.append(REAL).append(SPACE);
            } else if (type.equals(Byte.class)) {
                builder.append(BLOB).append(SPACE);
            }
            if (CamelCaseConverter.toUnderscore(field.getName()).equals(tableId)) {
                builder.append(PRIMARY_KEY).append(SPACE).append(AUTOINCREMENT);
            }
            if (position < fields.length- 1) {
                builder.append(",");
            }
            position++;
        }

        return format(TABLE_CREATE, tableName, builder.toString());
    }
}
