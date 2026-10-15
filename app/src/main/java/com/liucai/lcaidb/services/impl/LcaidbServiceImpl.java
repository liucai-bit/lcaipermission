package com.liucai.lcaidb.services.impl;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

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
import com.liucai.lcaidb.helper.LcaidbHelper;
import com.liucai.lcaidb.page.LcaiPage;
import com.liucai.lcaidb.services.service.LcaidbService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LcaidbService 默认实现。
 *
 * @author liucai
 */
public class LcaidbServiceImpl<T extends LcaiTableInfo>
        extends BaseCRUD<T> implements LcaidbService<T> {

    private final LcaidbHelper helper;
    /** 已存在表缓存，避免每次操作都查 sqlite_master */
    private final Set<String> existingTables = Collections.synchronizedSet(new HashSet<>());

    public LcaidbServiceImpl(Class<?> aClass) {
        super(aClass);
        this.helper = LcaidbHelper.getInstance();
        if (helper == null) {
            throw new LcaiHttpException("The database has not been initialized");
        }
    }

    // ---------------- 建表 ----------------

    @Override
    public void createTable(String sql) {
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "CREATE");
        if (tableIsExist()) {
            LcaiLogUtils.e("Table already exists, skip create.");
            return;
        }
        getWriteSqliteDatabase().execSQL(sql);
        existingTables.add(getTableName(entity.getClass()));
    }

    @Override
    public void createTableForClass() {
        if (tableIsExist()) {
            LcaiLogUtils.e("Table already exists, skip create.");
            return;
        }
        String sql = new Create<>(aClass).createTable();
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "CREATE");
        getWriteSqliteDatabase().execSQL(sql);
        existingTables.add(getTableName(entity.getClass()));
    }

    // ---------------- 插入 ----------------

    @Override
    public long insert(T entity) {
        if (!tableIsExist()) {
            LcaiLogUtils.e("Insert failed: table does not exist.");
            return -1L;
        }
        ContentValues values = new LcaidbContentValues<>(entity).getValues();
        LcaiLogUtils.w("SQL", values);
        return getWriteSqliteDatabase().insert(getTableName(entity.getClass()), null, values);
    }

    @Override
    public long batchInsert(List<T> entities) {
        if (!tableIsExist()) {
            LcaiLogUtils.e("batchInsert failed: table does not exist.");
            return 0L;
        }
        if (entities == null || entities.isEmpty()) return 0L;

        SQLiteDatabase db = getWriteSqliteDatabase();
        String tableName = getTableName(entity.getClass());
        int success = 0;
        db.beginTransaction();
        try {
            for (T item : entities) {
                ContentValues values = new LcaidbContentValues<>(item).getValues();
                long rowId = db.insert(tableName, null, values);
                if (rowId > 0) success++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        LcaiLogUtils.d("batchInsert 成功：" + success + " / " + entities.size());
        return success;
    }

    // ---------------- 删除 ----------------

    @Override
    public boolean deleteById(Collection<Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            LcaiLogUtils.e("deleteById: idList is empty.");
            return false;
        }
        if (!tableIsExist()) {
            LcaiLogUtils.e("deleteById failed: table does not exist.");
            return false;
        }
        String sql = new Delete<>(aClass).deleteById(idList);
        if (TextUtils.isEmpty(sql)) return false;
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "DELETE");
        try {
            getWriteSqliteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            LcaiLogUtils.e("deleteById failed", e);
            return false;
        }
    }

    @Override
    public boolean deleteByParams(Map<String, Object> condition) {
        if (condition == null || condition.isEmpty()) {
            LcaiLogUtils.e("deleteByParams: condition is empty.");
            return false;
        }
        if (!tableIsExist()) {
            LcaiLogUtils.e("deleteByParams failed: table does not exist.");
            return false;
        }
        String sql = new Delete<>(aClass).deleteByParams(condition);
        if (TextUtils.isEmpty(sql)) return false;
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "DELETE");
        try {
            getWriteSqliteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            LcaiLogUtils.e("deleteByParams failed", e);
            return false;
        }
    }

    @Override
    public boolean deleteByEntity(T entity) {
        if (entity == null) return false;
        if (!tableIsExist()) {
            LcaiLogUtils.e("deleteByEntity failed: table does not exist.");
            return false;
        }
        String sql = new Delete<>(aClass).deleteByEntity(entity);
        if (TextUtils.isEmpty(sql)) return false;
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "DELETE");
        try {
            getWriteSqliteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            LcaiLogUtils.e("deleteByEntity failed", e);
            return false;
        }
    }

    // ---------------- 更新 ----------------

    @Override
    public boolean updateById(T entity) {
        if (entity == null) return false;
        if (!tableIsExist()) {
            LcaiLogUtils.e("updateById failed: table does not exist.");
            return false;
        }
        String sql = new Update<>(aClass).updateById(entity);
        if (TextUtils.isEmpty(sql)) return false;
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "UPDATE");
        try {
            getWriteSqliteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            LcaiLogUtils.e("updateById failed", e);
            return false;
        }
    }

    @Override
    public boolean updateByParams(Map<String, Object> condition, Map<String, Object> update) {
        if (condition == null || condition.isEmpty()
                || update == null || update.isEmpty()) {
            LcaiLogUtils.e("updateByParams: condition or update is empty.");
            return false;
        }
        if (!tableIsExist()) {
            LcaiLogUtils.e("updateByParams failed: table does not exist.");
            return false;
        }
        String sql = new Update<>(aClass).updateByParams(condition, update);
        if (TextUtils.isEmpty(sql)) return false;
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "UPDATE");
        try {
            getWriteSqliteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            LcaiLogUtils.e("updateByParams failed", e);
            return false;
        }
    }

    // ---------------- 查询 ----------------

    @Override
    public List<T> query() {
        if (!tableIsExist()) {
            LcaiLogUtils.e("query failed: table does not exist.");
            return Collections.emptyList();
        }
        Query<T> query = new Query<>(aClass);
        String sql = query.queryList();
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        List<T> result = new ArrayList<>();
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                result.add(query.getEntity(cursor, aClass));
            }
        }
        return result;
    }

    @Override
    public T queryById(T entity) {
        if (entity == null || !tableIsExist()) {
            LcaiLogUtils.e("queryById failed: entity is null or table does not exist.");
            return null;
        }
        Map<String, Object> entityValue = new DBUtils<>().getEntityMap(entity);
        String tableId = getTableId(aClass);
        Object value = entityValue.get(tableId);
        if (value == null || TextUtils.isEmpty(String.valueOf(value))) {
            tableId = CamelCaseConverter.underscoreToCamel(tableId);
            value = entityValue.get(tableId);
        }
        if (value == null) return null;

        Map<String, Object> condition = new HashMap<>(1);
        condition.put(tableId, value);

        Query<T> query = new Query<>(aClass);
        String sql = query.queryByParams(condition);
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        T result = null;
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                result = query.getEntity(cursor, aClass);
            }
        }
        return result;
    }

    @Override
    public List<T> queryParams(Map<String, Object> condition) {
        if (condition == null || condition.isEmpty()) {
            LcaiLogUtils.e("queryParams: condition is empty.");
            return Collections.emptyList();
        }
        if (!tableIsExist()) {
            LcaiLogUtils.e("queryParams failed: table does not exist.");
            return Collections.emptyList();
        }
        Query<T> query = new Query<>(aClass);
        String sql = query.queryByParams(condition);
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        List<T> result = new ArrayList<>();
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                result.add(query.getEntity(cursor, aClass));
            }
        }
        return result;
    }

    @Override
    public List<T> queryByPage(LcaiPage<T> page, String order, OrderTypeEnum orderType) {
        if (!tableIsExist()) {
            LcaiLogUtils.e("queryByPage failed: table does not exist.");
            return Collections.emptyList();
        }
        Query<T> query = new Query<>(aClass);
        String sql = query.queryByPage(page.getStart(), page.getEnd(), order, orderType);
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        List<T> result = new ArrayList<>();
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                result.add(query.getEntity(cursor, aClass));
            }
        }
        return result;
    }

    @Override
    public List<T> queryByPageParams(LcaiPage<T> page, String order,
                                     OrderTypeEnum orderType, Map<String, Object> condition) {
        if (!tableIsExist()) {
            LcaiLogUtils.e("queryByPageParams failed: table does not exist.");
            return Collections.emptyList();
        }
        Query<T> query = new Query<>(aClass);
        String sql = query.queryByPageParams(page.getStart(), page.getEnd(), order, orderType, condition);
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        List<T> result = new ArrayList<>();
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                result.add(query.getEntity(cursor, aClass));
            }
        }
        return result;
    }

    @SuppressLint("Range")
    @Override
    public long queryCount() {
        if (!tableIsExist()) {
            LcaiLogUtils.e("queryCount failed: table does not exist.");
            return 0L;
        }
        String sql = new Query<>(aClass).queryCount();
        LcaiLogUtils.w("SQL", sql);
        checkSqlPrefix(sql, "SELECT");
        long count = 0L;
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                count = cursor.getLong(cursor.getColumnIndex(LcaiSql.COUNT));
            }
        }
        return count;
    }

    // ---------------- 数据库句柄 ----------------

    @Override
    public SQLiteDatabase getWriteSqliteDatabase() {
        return helper.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadSqliteDatabase() {
        return helper.getReadableDatabase();
    }

    // ---------------- 私有辅助 ----------------

    /**
     * 表是否存在。命中缓存后直接返回，避免每次查 sqlite_master。
     */
    private boolean tableIsExist() {
        String table = getTableName(entity.getClass());
        if (existingTables.contains(table)) return true;

        String sql = format(TABLE_EXIST, table);
        LcaiLogUtils.w("SQL", sql);
        try (Cursor cursor = getReadSqliteDatabase().rawQuery(sql, null)) {
            if (cursor.moveToNext()) {
                existingTables.add(table);
                return true;
            }
        }
        return false;
    }

    private void checkSqlPrefix(@NonNull String sql, @NonNull String prefix) {
        if (!sql.startsWith(prefix)) {
            throw new LcaiHttpException("Only " + prefix + " queries are allowed");
        }
    }
}