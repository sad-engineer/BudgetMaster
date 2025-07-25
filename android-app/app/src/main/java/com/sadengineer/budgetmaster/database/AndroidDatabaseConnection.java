// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Android реализация DatabaseConnection для работы с SQLite
 */
public class AndroidDatabaseConnection implements com.sadengineer.budgetmaster.backend.database.DatabaseConnection {
    
    private final SQLiteDatabase database;
    private final Context context;
    
    /**
     * Конструктор для Android соединения
     * @param context Android контекст
     * @param dbPath путь к базе данных
     */
    public AndroidDatabaseConnection(Context context, String dbPath) {
        this.context = context;
        this.database = context.openOrCreateDatabase(dbPath, Context.MODE_PRIVATE, null);
        
        // Устанавливаем кодировку UTF-8
        database.execSQL("PRAGMA encoding = 'UTF-8'");
    }
    
    @Override
    public void executeUpdate(String sql, Object... params) {
        SQLiteStatement statement = database.compileStatement(sql);
        
        try {
            // Привязываем параметры
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    statement.bindNull(i + 1);
                } else if (param instanceof String) {
                    statement.bindString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    statement.bindLong(i + 1, (Integer) param);
                } else if (param instanceof Long) {
                    statement.bindLong(i + 1, (Long) param);
                } else if (param instanceof Double) {
                    statement.bindDouble(i + 1, (Double) param);
                } else if (param instanceof Boolean) {
                    statement.bindLong(i + 1, (Boolean) param ? 1L : 0L);
                } else {
                    statement.bindString(i + 1, param.toString());
                }
            }
            
            statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
    }
    
    @Override
    public <T> List<T> executeQuery(String sql, Function<ResultRow, T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        
        try (Cursor cursor = database.rawQuery(sql, convertParamsToStringArray(params))) {
            while (cursor.moveToNext()) {
                AndroidResultRow row = new AndroidResultRow(cursor);
                T result = mapper.apply(row);
                if (result != null) {
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    @Override
    public <T> Optional<T> executeQuerySingle(String sql, Function<ResultRow, T> mapper, Object... params) {
        try (Cursor cursor = database.rawQuery(sql, convertParamsToStringArray(params))) {
            if (cursor.moveToFirst()) {
                AndroidResultRow row = new AndroidResultRow(cursor);
                T result = mapper.apply(row);
                return Optional.ofNullable(result);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
    
    /**
     * Конвертирует параметры в массив строк для Android Cursor
     */
    private String[] convertParamsToStringArray(Object... params) {
        if (params == null || params.length == 0) {
            return null;
        }
        
        String[] stringParams = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            stringParams[i] = params[i] != null ? params[i].toString() : null;
        }
        return stringParams;
    }
    
    /**
     * Android реализация ResultRow
     */
    private static class AndroidResultRow implements com.sadengineer.budgetmaster.backend.database.ResultRow {
        private final Cursor cursor;
        
        public AndroidResultRow(Cursor cursor) {
            this.cursor = cursor;
        }
        
        @Override
        public String getString(int columnIndex) {
            return cursor.getString(columnIndex);
        }
        
        @Override
        public String getString(String columnName) {
            return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
        }
        
        @Override
        public Integer getInt(int columnIndex) {
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
        }
        
        @Override
        public Integer getInt(String columnName) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
        }
        
        @Override
        public Long getLong(int columnIndex) {
            return cursor.isNull(columnIndex) ? null : cursor.getLong(columnIndex);
        }
        
        @Override
        public Long getLong(String columnName) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getLong(columnIndex);
        }
        
        @Override
        public Double getDouble(int columnIndex) {
            return cursor.isNull(columnIndex) ? null : cursor.getDouble(columnIndex);
        }
        
        @Override
        public Double getDouble(String columnName) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getDouble(columnIndex);
        }
        
        @Override
        public Boolean getBoolean(int columnIndex) {
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex) != 0;
        }
        
        @Override
        public Boolean getBoolean(String columnName) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex) != 0;
        }
        
        @Override
        public Object getObject(int columnIndex) {
            return cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
        }
        
        @Override
        public Object getObject(String columnName) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
        }
    }
} 