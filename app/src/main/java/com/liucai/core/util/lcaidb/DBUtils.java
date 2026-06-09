package com.liucai.core.util.lcaidb;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DBUtils<T> {
    public Map<String, Object> getEntityMap(T entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Field[] fields = getFields(entity.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            String name=field.getName();
            try {
                map.put(name, field.get(entity));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public Field[] getFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }
}
