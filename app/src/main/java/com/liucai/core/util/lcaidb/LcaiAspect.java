package com.liucai.core.util.lcaidb;

public class LcaiAspect {

    public static String getTableNameValue(Class<?> arg1) {
        String mTableName="";
        if (arg1.isAnnotationPresent(TableName.class)) {
            TableName tableName = arg1.getAnnotation(TableName.class);
            mTableName = tableName.tableName();
        }
        return mTableName;
    }

    public static String getTableIdValue(Class<?> arg1) {
        String mTableId="";
        if (arg1.isAnnotationPresent(TableId.class)) {
            TableId tableId = arg1.getAnnotation(TableId.class);
            mTableId = tableId.tableId();
        }
        return mTableId;
    }


}
