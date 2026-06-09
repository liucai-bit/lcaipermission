package com.liucai.lcaidb.services.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.liucai.core.exception.LcaiHttpException;
import com.liucai.core.util.lcaidb.CamelCaseConverter;
import com.liucai.core.util.lcaidb.DBUtils;
import com.liucai.core.util.lcaidb.LcaidbContentValues;
import com.liucai.core.util.log.LcaiLogUtils;
import com.liucai.core.util.text.TextUtils;
import com.liucai.lcaidb.base.BaseCRUD;
import com.liucai.lcaidb.base.module.LcaiTableInfo;
import com.liucai.lcaidb.base.sql.LcaiSql;
import com.liucai.lcaidb.crud.Create;
import com.liucai.lcaidb.crud.Delete;
import com.liucai.lcaidb.crud.Query;
import com.liucai.lcaidb.crud.Update;
import com.liucai.lcaidb.enu.OrderTypeEnum;
import com.liucai.lcaidb.helper.LcaiDbResult;
import com.liucai.lcaidb.helper.LcaidbHelper;
import com.liucai.lcaidb.page.LcaiPage;
import com.liucai.lcaidb.services.service.LcaidbService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liucai
 * @program lcpermission
 * @description
 * @Date 2026/6/8
 */
public class LcaidbServiceImpl<T extends LcaiTableInfo> extends BaseCRUD<T> implements LcaidbService<T> {

    LcaidbHelper helper;

    public LcaidbServiceImpl(Class<?> aClass) {
        super(aClass);
        this.helper = LcaidbHelper.getInstance();
        if (helper == null) {
            throw new LcaiHttpException("The database has not been initialized");
        }
        this.helper = helper;
    }

    /**
     * create table with Entity
     * @param sql
     */
    @Override
    public void createTable(String sql) {
        LcaiLogUtils.w("SQL", sql);
        if (!sql.startsWith("CREATE")) {
            throw new LcaiHttpException("Only CREATE queries are allowed via execSQL");
        }
        if (!tableIsExist()) {
            getWriteSqliteDatabase().execSQL(sql);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is exist!");
        }

    }

    @Override
    public void createTableForClass() {
        if (!tableIsExist()) {
            String sql = new Create<>(aClass).createTable();
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("CREATE")) {
                throw new LcaiHttpException("Only CREATE queries are allowed via execSQL");
            }
            getWriteSqliteDatabase().execSQL(sql);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is exist!");
        }
    }

    /**
     * insert into table
     * @return
     */
    @Override
    public long insert(T entity) {
        if (tableIsExist()) {
            LcaidbContentValues contentValues = new LcaidbContentValues<>(entity);
            ContentValues values = contentValues.getValues();
            LcaiLogUtils.w("SQL", values);
            return getReadSqliteDatabase().insert(getTableName(entity.getClass()), null,values);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return 0L;
    }

    @Override
    public long batchInsert(List<T> entitys) {
        int success = 0;
        int failure = 0;
        if (tableIsExist()) {
            if (entitys != null && entitys.size() > 0) {
                for (T entity : entitys) {
                    LcaidbContentValues contentValues = new LcaidbContentValues<>(entity);
                    ContentValues values = contentValues.getValues();
                    LcaiLogUtils.w("SQL", values);
                    long insert = getReadSqliteDatabase().insert(getTableName(entity.getClass()), null, values);
                    if (insert > 0) {
                        success++;
                    } else {
                        failure++;
                    }
                }
            }
        }
        LcaiLogUtils.d("插入成功："+success,"插入失败："+failure);
        return success;
    }

    @Override
    public boolean deleteById(Collection<Serializable> idList) {
        if (tableIsExist()) {
            String sql = new Delete<>(aClass).deleteById(idList);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("DELETE")) {
                throw new LcaiHttpException("Only DELETE queries are allowed via execSQL");
            }
            try {
                getWriteSqliteDatabase().execSQL(sql);
                LcaiLogUtils.i("delete success!");
            } catch (Exception e) {
                return LcaiDbResult.retBool(-1);
            }
            return LcaiDbResult.retBool(1);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return LcaiDbResult.retBool(1);
    }

    @Override
    public boolean deleteByParams(Map<String, Object> condition){
        if (tableIsExist()) {
            String sql = new Delete<>(aClass).deleteByParams(condition);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("DELETE")) {
                throw new LcaiHttpException("Only DELETE queries are allowed via execSQL");
            }
            try {
                getWriteSqliteDatabase().execSQL(sql);
                LcaiLogUtils.i("delete success!");
            } catch (Exception e) {
                return LcaiDbResult.retBool(-1);
            }
            return LcaiDbResult.retBool(1);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return LcaiDbResult.retBool(1);
    }

    @Override
    public boolean deleteByEntity(T entity) {
        if (tableIsExist()) {
            String sql = new Delete<>(aClass).deleteByEntity(entity);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("DELETE")) {
                throw new LcaiHttpException("Only DELETE queries are allowed via execSQL");
            }
            try {
                getWriteSqliteDatabase().execSQL(sql);
                LcaiLogUtils.i("delete success!");
            } catch (Exception e) {
                return LcaiDbResult.retBool(-1);
            }
        }else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return LcaiDbResult.retBool(1);
    }

    @Override
    public boolean updateById(T entity) {
        if (tableIsExist()) {
            String sql = new Update<>(aClass).updateById(entity);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("UPDATE")) {
                throw new LcaiHttpException("Only UPDATE queries are allowed via execSQL");
            }
            try {
                getWriteSqliteDatabase().execSQL(sql);
                LcaiLogUtils.i("update success!");
            } catch (Exception e) {
                LcaiLogUtils.e(e.getMessage());
                return LcaiDbResult.retBool(-1);
            }
            return LcaiDbResult.retBool(1);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return LcaiDbResult.retBool(1);
    }

    @Override
    public boolean updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        if (tableIsExist()) {
            String sql = new Update<>(aClass).updateByParams(condition, update);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("UPDATE")) {
                throw new LcaiHttpException("Only UPDATE queries are allowed via execSQL");
            }
            try {
                getWriteSqliteDatabase().execSQL(sql);
                LcaiLogUtils.i("update success!");
            } catch (Exception e) {
                return LcaiDbResult.retBool(-1);
            }
            return LcaiDbResult.retBool(1);
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return LcaiDbResult.retBool(1);
    }

    @Override
    public List<T> query() {
        if (tableIsExist()) {
            List<T> tList = new ArrayList<>();
            Query<T> query = new Query<>(aClass);
            String sql = query.queryList();
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                T entity1 = query.getEntity(cursor, aClass);
                tList.add(entity1);
            }
            return tList;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return new ArrayList<>();
    }

    @Override
    public T queryById(T entity) {
        if (tableIsExist()) {
            T entity1 = null;
            Map<String, Object> entityValue = new DBUtils<>().getEntityMap(entity);
            Map<String, Object> queryMap = new HashMap<>();
            String tableId = getTableId(aClass);
            Object value = entityValue.get(tableId);
            if (TextUtils.isEmpty(value + "")) {
                tableId = CamelCaseConverter.underscoreToCamel(tableId);
                value = entityValue.get(tableId);
            }
            queryMap.put(tableId, value);
            Query<T> query = new Query(aClass);
            String sql = query.queryByParams(queryMap);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                entity1 = query.getEntity(cursor, aClass);
            }
            return entity1;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return null;
    }

    @Override
    public List<T> queryParams(Map<String, Object> condition) {
        if (tableIsExist()) {
            List<T> tList = new ArrayList<>();
            Query<T> query = new Query<>(aClass);
            String sql = query.queryByParams(condition);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                T entity1 = query.getEntity(cursor, aClass);
                tList.add(entity1);
            }
            return tList;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return new ArrayList<>();
    }

    @Override
    public List<T> qyeryByPage(LcaiPage<T> page, String order, OrderTypeEnum ordertype) {
        if (tableIsExist()) {
            List<T> tList = new ArrayList<>();
            Query<T> query = new Query<>(aClass);
            String sql = query.queryByPage(page.getStart(), page.getEnd(), order, ordertype);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                T entity = query.getEntity(cursor, aClass);
                tList.add(entity);
            }
            return tList;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return new ArrayList<>();
    }

    @Override
    public List<T> queryByPageParams(LcaiPage<T> page, String order, OrderTypeEnum ordertype, Map<String, Object> condition) {
        if (tableIsExist()) {
            List<T> tList = new ArrayList<>();
            Query<T> query = new Query<>(aClass);
            String sql = query.queryByPageParams(page.getStart(), page.getEnd(), order, ordertype, condition);
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                T entity = query.getEntity(cursor, aClass);
                tList.add(entity);
            }
            return tList;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return new ArrayList<>();
    }

    @SuppressLint("Range")
    @Override
    public long queryCount() {
        if (tableIsExist()) {
            long count = 0;
            Query query = new Query(aClass);
            String sql = query.queryCount();
            LcaiLogUtils.w("SQL", sql);
            if (!sql.startsWith("SELECT")) {
                throw new LcaiHttpException("Only SELECT queries are allowed via rawQuery");
            }
            Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                count = cursor.getLong(cursor.getColumnIndex(LcaiSql.COUNT));
            }
            return count;
        } else {
            LcaiLogUtils.e("execSQL failed,because table is not exist!");
        }
        return 0;
    }

    @Override
    public SQLiteDatabase getWriteSqliteDatabase() {
        return helper.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadSqliteDatabase() {
        return helper.getReadableDatabase();
    }

    public boolean tableIsExist() {
        String sql = format(TABLE_EXIST, new Object[]{getTableName(entity.getClass())});
        LcaiLogUtils.w(sql);
        Cursor cursor = getReadSqliteDatabase().rawQuery(sql, new String[]{});
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }
}
