package com.liucai.core.util.lcaidb;

import android.content.ContentValues;

import com.liucai.core.util.text.TextUtils;

import java.util.Map;

public class LcaidbContentValues<T> {

    private final T entity;

    public LcaidbContentValues(T entity) {
        this.entity = entity;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        Map<String, Object> entityMap = new DBUtils<T>().getEntityMap(entity);
        if (entityMap == null) {
            return values;
        }

        for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            String column = CamelCaseConverter.toUnderscore(name);

            if (value instanceof String) {
                String stringValue = (String) value;
                if (!TextUtils.isEmpty(stringValue)) {
                    values.put(column, stringValue);
                }
            } else if (value instanceof Integer) {
                values.put(column, (Integer) value);
            } else if (value instanceof Long) {
                values.put(column, (Long) value);
            } else if (value instanceof Short) {
                values.put(column, (Short) value);
            } else if (value instanceof Byte) {
                values.put(column, (Byte) value);
            } else if (value instanceof Float) {
                values.put(column, (Float) value);
            } else if (value instanceof Double) {
                values.put(column, (Double) value);
            } else if (value instanceof Boolean) {
                values.put(column, (Boolean) value);
            } else if (value instanceof byte[]) {
                values.put(column, (byte[]) value);
            } else {
                String stringValue = value.toString();
                if (!TextUtils.isEmpty(stringValue)) {
                    values.put(column, stringValue);
                }
            }
        }

        return values;
    }
}