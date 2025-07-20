package com.example.budgetmaster.backend.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Класс для работы с SQLite базой данных в Android
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "budgetmaster.db";
    private static final int DATABASE_VERSION = 1;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public DatabaseHelper(Context context, String dbName, Object factory, int version) {
        super(context, dbName, (SQLiteDatabase.CursorFactory) factory, version);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблиц будет выполнено отдельно
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Обновление схемы базы данных
    }
} 