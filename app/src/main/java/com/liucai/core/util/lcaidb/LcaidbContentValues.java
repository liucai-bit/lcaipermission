package com.liucai.core.util.lcaidb;

import android.content.ContentValues;

import com.liucai.core.util.text.TextUtils;

import java.util.Map;



public class LcaidbContentValues<T> {

    private T entity;


    public LcaidbContentValues(T entity) {
        this.entity = entity;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        Map<String,Object> entityMap = new DBUtils().getEntityMap(entity);
        for (Map.Entry<String,Object> entry : entityMap.entrySet()) {
            String name = entry.getKey();
            if (!TextUtils.isEmpty(entry.getValue() + "")) {
                String value = entry.getValue().toString();
                values.put(CamelCaseConverter.toUnderscore(name),value);
            }
        }
        return values;
    }
}
