package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
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
public class Update<T extends LcaiTableInfo> extends BaseCRUD<T> {
    public Update(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 通过ID更新数据
     * @param entity
     * @return
     */
    public String updateById(T entity) {
        if (entity == null) {
            return "";
        }

        Map<String, Object> entityMap = new DBUtils<>().getEntityMap(entity);
        String tableId = getTableId(aClass);
        String key = CamelCaseConverter.underscoreToCamel(tableId);
        Object tid = entityMap.get(key);
        if (TextUtils.isEmpty(tid+"")) {
            tid = entityMap.get(tableId);
        }else{
            tableId = key;
        }

        if (TextUtils.isEmpty(tid + "")) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.getName().equals(tid)) {
                if (entityMap.get(field.getName())!=null) {
                    String buliderString = builder.toString();
                    if (!TextUtils.isEmpty(buliderString)) {
                        builder.append(",");
                    }
                    Object value = entityMap.get(field.getName());
                    if (!TextUtils.isEmpty(value+"")) {
                        builder.append(CamelCaseConverter.toUnderscore(field.getName())).append("=");
                        if (value instanceof String) {
                            builder.append(format(STRING, value));
                        } else{
                            builder.append(value);
                        }
                    }
                }

            }
        }
        return format(UPDATE_BY_ID, getTableName(entity.getClass()), builder.toString(),tableId, tid);
    }

    /**
     * 通过条件更新数据
     * @param condition
     * @param update
     * @return
     */
    public String updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        StringBuilder set = new StringBuilder();
        int setposition=0;
        for (Map.Entry<String, Object> entry : update.entrySet()) {
            set.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            if (entry.getValue() instanceof String) {
                set.append(format(STRING, entry.getValue().toString()));
            } else {
                set.append(entry.getValue());
            }
            if (setposition < update.size() - 1) {
                set.append(",");
            }
            setposition++;
        }

        int conposition = 0;
        StringBuilder con = new StringBuilder();
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            con.append(CamelCaseConverter.toUnderscore(entry.getKey())).append("=");
            if (entry.getValue() instanceof String) {
                con.append(format(STRING, entry.getValue().toString()));
            } else {
                con.append(entry.getValue());
            }

            con.append(SPACE);
            if (conposition < condition.size() - 1) {
                con.append(AND).append(SPACE);
            }
            conposition++;
        }
        return format(UPDATE_BY_PARAMS,getTableName(entity.getClass()), set.toString(), con.toString());
    }
}
