package com.example.budgetmaster.backend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация DatabaseConnection для Android SQLite
 */
public class AndroidDatabaseConnection implements DatabaseConnection {
    
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    
    public AndroidDatabaseConnection(Context context, String dbName) {
        this.dbHelper = new DatabaseHelper(context, dbName, null, 1);
        this.database = dbHelper.getWritableDatabase();
    }
    
    @Override
    public <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        
        try (Cursor cursor = database.rawQuery(sql, convertParamsToStringArray(params))) {
            AndroidResultRow resultRow = new AndroidResultRow(cursor);
            
            while (resultRow.next()) {
                T item = mapper.mapRow(resultRow);
                if (item != null) {
                    results.add(item);
                }
            }
        }
        
        return results;
    }
    
    @Override
    public <T> Optional<T> executeQuerySingle(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = executeQuery(sql, mapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public long executeInsert(String sql, Object... params) {
        // Для INSERT используем ContentValues
        ContentValues values = createContentValuesFromParams(sql, params);
        return database.insert("currencies", null, values);
    }
    
    @Override
    public int executeUpdate(String sql, Object... params) {
        // Для UPDATE используем ContentValues
        ContentValues values = createContentValuesFromParams(sql, params);
        return database.update("currencies", values, "id = ?", new String[]{params[0].toString()});
    }
    
    @Override
    public int executeDelete(String sql, Object... params) {
        return database.delete("currencies", "id = ?", new String[]{params[0].toString()});
    }
    
    @Override
    public void beginTransaction() {
        database.beginTransaction();
    }
    
    @Override
    public void commitTransaction() {
        database.setTransactionSuccessful();
        database.endTransaction();
    }
    
    @Override
    public void rollbackTransaction() {
        database.endTransaction();
    }
    
    @Override
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    private String[] convertParamsToStringArray(Object... params) {
        if (params == null) {
            return null;
        }
        
        String[] stringParams = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            stringParams[i] = params[i] != null ? params[i].toString() : null;
        }
        return stringParams;
    }
    
    private ContentValues createContentValuesFromParams(String sql, Object... params) {
        ContentValues values = new ContentValues();
        
        // Простая реализация для валют
        if (params.length > 0 && params[0] instanceof String) {
            values.put("title", (String) params[0]);
        }
        if (params.length > 1 && params[1] instanceof Integer) {
            values.put("position", (Integer) params[1]);
        }
        
        return values;
    }
    
    /**
     * Реализация ResultRow для Android Cursor
     */
    private static class AndroidResultRow implements ResultRow {
        
        private final Cursor cursor;
        
        public AndroidResultRow(Cursor cursor) {
            this.cursor = cursor;
        }
        
        @Override
        public Integer getInt(String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
        }
        
        @Override
        public String getString(String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex);
        }
        
        @Override
        public Double getDouble(String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getDouble(columnIndex);
        }
        
        @Override
        public Boolean getBoolean(String columnName) {
            int columnIndex = cursor.getColumnIndex(columnName);
            return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex) == 1;
        }
        
        @Override
        public java.time.LocalDateTime getDateTime(String columnName) {
            String dateString = getString(columnName);
            if (dateString == null) {
                return null;
            }
            try {
                return java.time.LocalDateTime.parse(dateString);
            } catch (Exception e) {
                return null;
            }
        }
        
        @Override
        public boolean next() {
            return cursor.moveToNext();
        }
        
        @Override
        public void close() {
            cursor.close();
        }
    }
} 