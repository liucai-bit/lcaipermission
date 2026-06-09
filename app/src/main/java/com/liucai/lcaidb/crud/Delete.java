package com.liucai.lcaidb.crud;

import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class Delete<T extends LcaiTableInfo> extends BaseCRUD<T> {


    public Delete(Class<?> aClass) {
        super(aClass);
    }

    /**
     * 根据ID删除数据
     * 但数据，多数据皆可删除
     * @param idList
     * @return sql 语句
     */
    public String deleteById(Collection<Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return "";
        }
        String s1 = "";
        for (Serializable s : idList) {
            s1 += s + ",";
        }
        return format(DELETE_BYID, getTableName(entity.getClass()), getTableId(entity.getClass()), s1.substring(0, s1.length() - 1));
    }

    /**
     * 根据条件删除
     * @param condition
     * Map<列名,条件>
     * Map<id,1>
     * @return sql 语句
     */
    public String deleteByParams(Map<String, Object> condition) {
        StringBuilder builder = new StringBuilder();
        int position = 0;
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(SPACE);
            if (position < condition.size() - 1) {
                builder.append(AND).append(SPACE);
            }
            position++;
        }
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());
    }

    /**
     * 根据实体删除
     * @return
     */
    public String deleteByEntity(T entity) {
        StringBuilder builder = new StringBuilder();
        String builderString = builder.toString();
        Map<String, Object> deleteByEntity = new DBUtils<>().getEntityMap(entity);
        for (Map.Entry<String, Object> entry : deleteByEntity.entrySet()) {
            if (TextUtils.isEmpty(builderString)) {
                builder.append(AND).append(SPACE);
            }
            String name = entry.getKey();
            Object value = entry.getValue();
            if (!TextUtils.isEmpty(value + "")) {
                builder.append(name).append("=").append(value).append(SPACE);
            }
        }
        return format(DELETE_BY_PARAMS, getTableName(entity.getClass()), builder.toString());

    }
}
