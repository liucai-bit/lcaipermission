package com.liucai.core.util.lcaidb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtils<T> {

    public Map<String, Object> getEntityMap(T entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Field[] fields = getFields(entity.getClass());

        for (Field field : fields) {
            // 过滤静态字段、合成字段、以及 transient（瞬态）字段
            if (field.isSynthetic()
                    || Modifier.isStatic(field.getModifiers())
                    || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String name = field.getName();

            try {
                map.put(name, field.get(entity));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    /**
     * 获取所有字段（包含当前类及所有父类，直到 Object）
     */
    public Field[] getFields(Class<?> clazz) {
        List<Field> allFields = new ArrayList<>();
        Class<?> current = clazz;

        // 向上遍历继承树
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                allFields.add(field);
            }
            current = current.getSuperclass();
        }

        return allFields.toArray(new Field[0]);
    }
}