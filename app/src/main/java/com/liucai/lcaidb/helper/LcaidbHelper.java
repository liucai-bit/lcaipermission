package com.liucai.lcaidb.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liucai.core.util.log.LcaiLogUtils;

/**
 * SQLite 数据库 Helper。单例，通过 {@link #init(Context, String)} 初始化。
 *
 * <p>线程安全由 {@link SQLiteOpenHelper} 自身保证，无需外部加锁。
 *
 * @author liucai
 */
public class LcaidbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final int DEFAULT_PAGE_SIZE = 1024;

    private static volatile LcaidbHelper instance;

    public static void init(@NonNull Context context, @NonNull String dbName) {
        if (instance == null) {
            synchronized (LcaidbHelper.class) {
                if (instance == null) {
                    instance = new LcaidbHelper(context.getApplicationContext(), dbName);
                }
            }
        }
    }

    @Nullable
    public static LcaidbHelper getInstance() {
        return instance;
    }

    private LcaidbHelper(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);
        LcaiLogUtils.d("Database helper initialized: " + dbName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 表由各业务模块通过 LcaidbCore.createTableForClass() 显式创建
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setPageSize(DEFAULT_PAGE_SIZE);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LcaiLogUtils.d("Upgrading database from version " + oldVersion + " to " + newVersion);
    }

    @Override
    public synchronized void close() {
        super.close();
        instance = null;
    }
}