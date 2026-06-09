package com.liucai.lcaidb.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.liucai.core.util.log.LcaiLogUtils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LcaidbHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "app_database.db";
    private static final int DEFAULT_PAGE_SIZE = 1024; // 默认页大小
    private static final String JOURNAL_MODE = "WAL"; // 日志模式
    private static final String SYNC_MODE = "NORMAL"; // 同步模式
    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;
    public static volatile LcaidbHelper instance;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void init(Context mContext, String dbName) {
        if (instance == null) {
            synchronized (LcaidbHelper.class) {
                if (instance == null) {
                    instance = new LcaidbHelper(mContext.getApplicationContext(), dbName);
                }
            }
        }
    }

    public static LcaidbHelper getInstance() {
        return instance;
    }

    private LcaidbHelper(Context mContext,String dbName) {
        super(mContext,dbName, null, DB_VERSION);
        LcaiLogUtils.d("Database helper initialized: " + dbName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // 配置数据库参数
        db.setPageSize(DEFAULT_PAGE_SIZE);
//        db.execSQL("PRAGMA journal_mode = " + JOURNAL_MODE);
//        db.execSQL("PRAGMA synchronous = " + SYNC_MODE);
        // 启用外键约束
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LcaiLogUtils.d("Upgrading database from version " + oldVersion + " to " + newVersion);
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        lock.readLock().lock();
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                readableDatabase = super.getReadableDatabase();
                readableDatabase.enableWriteAheadLogging();
            }
            return readableDatabase;
        }finally {
            lock.readLock().unlock();
        }

    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        lock.writeLock().lock();
        try {
            if (writableDatabase == null || !writableDatabase.isOpen()) {
                writableDatabase = super.getWritableDatabase();
            }
            return writableDatabase;
        } catch (SQLException e) {
            LcaiLogUtils.e("Failed to open writable database", e);
        }finally {
            lock.writeLock().unlock();
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            closeDatabase();
        }finally {
            super.finalize();
        }
    }

    public synchronized void closeDatabase() {
        if (readableDatabase != null && readableDatabase.isOpen()) {
            readableDatabase.close();
            readableDatabase = null;
        }
        if (writableDatabase != null && writableDatabase.isOpen()) {
            writableDatabase.close();
            writableDatabase = null;
        }
    }



}
