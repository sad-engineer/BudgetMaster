package com.sadengineer.budgetmaster.backend;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Android адаптер для работы с SQLite
 * Реализация для Android приложения
 */
public class AndroidDatabaseAdapter {
    
    private static final String TAG = "AndroidDatabaseAdapter";
    private SQLiteDatabase database;
    private String databasePath;
    
    public void connect(String path) {
        try {
            databasePath = path;
            database = SQLiteDatabase.openOrCreateDatabase(path, null);
            Log.i(TAG, "Android: Подключение к базе данных успешно: " + path);
        } catch (SQLiteException e) {
            Log.e(TAG, "Android: Ошибка подключения к базе данных: " + e.getMessage());
            throw new RuntimeException("Не удалось подключиться к базе данных", e);
        }
    }
    
    public void executeSQL(String sql) {
        if (!isConnected()) {
            throw new RuntimeException("Соединение с базой данных не установлено");
        }
        
        try {
            database.execSQL(sql);
            Log.i(TAG, "Android: Выполнен SQL запрос: " + sql);
        } catch (SQLiteException e) {
            Log.e(TAG, "Android: Ошибка выполнения SQL запроса: " + e.getMessage());
            throw new RuntimeException("Ошибка выполнения SQL запроса", e);
        }
    }
    
    public Cursor query(String sql) {
        if (!isConnected()) {
            throw new RuntimeException("Соединение с базой данных не установлено");
        }
        
        try {
            Cursor cursor = database.rawQuery(sql, null);
            Log.i(TAG, "Android: Выполнен запрос: " + sql);
            return cursor;
        } catch (SQLiteException e) {
            Log.e(TAG, "Android: Ошибка выполнения запроса: " + e.getMessage());
            throw new RuntimeException("Ошибка выполнения запроса", e);
        }
    }
    
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.i(TAG, "Android: Соединение с базой данных закрыто");
        }
    }
    
    public boolean isConnected() {
        return database != null && database.isOpen();
    }
    
    /**
     * Простой адаптер для конвертации Android Cursor в Java ResultSet
     * Реализует только основные методы
     */
    public static class SimpleResultSet {
        private final Cursor cursor;
        private boolean isClosed = false;
        
        public SimpleResultSet(Cursor cursor) {
            this.cursor = cursor;
        }
        
        public boolean next() {
            if (isClosed) return false;
            return cursor.moveToNext();
        }
        
        public String getString(String columnLabel) {
            int columnIndex = cursor.getColumnIndex(columnLabel);
            return cursor.getString(columnIndex);
        }
        
        public int getInt(String columnLabel) {
            int columnIndex = cursor.getColumnIndex(columnLabel);
            return cursor.getInt(columnIndex);
        }
        
        public double getDouble(String columnLabel) {
            int columnIndex = cursor.getColumnIndex(columnLabel);
            return cursor.getDouble(columnIndex);
        }
        
        public void close() {
            if (!isClosed) {
                cursor.close();
                isClosed = true;
            }
        }
        
        public boolean wasNull() { return false; }
        public String getString(int columnIndex) { return cursor.getString(columnIndex); }
        public boolean getBoolean(String columnLabel) { return cursor.getInt(cursor.getColumnIndex(columnLabel)) != 0; }
        public byte getByte(String columnLabel) { return (byte) cursor.getInt(cursor.getColumnIndex(columnLabel)); }
        public short getShort(String columnLabel) { return cursor.getShort(cursor.getColumnIndex(columnLabel)); }
        public int getInt(int columnIndex) { return cursor.getInt(columnIndex); }
        public long getLong(String columnLabel) { return cursor.getLong(cursor.getColumnIndex(columnLabel)); }
        public float getFloat(String columnLabel) { return cursor.getFloat(cursor.getColumnIndex(columnLabel)); }
        public double getDouble(int columnIndex) { return cursor.getDouble(columnIndex); }
        public java.math.BigDecimal getBigDecimal(String columnLabel) { return new java.math.BigDecimal(cursor.getString(cursor.getColumnIndex(columnLabel))); }
        public java.sql.Date getDate(String columnLabel) { return java.sql.Date.valueOf(cursor.getString(cursor.getColumnIndex(columnLabel))); }
        public java.sql.Time getTime(String columnLabel) { return java.sql.Time.valueOf(cursor.getString(cursor.getColumnIndex(columnLabel))); }
        public java.sql.Timestamp getTimestamp(String columnLabel) { return java.sql.Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(columnLabel))); }
        
        // Остальные методы возвращают null или выбрасывают исключение
        public java.io.InputStream getAsciiStream(String columnLabel) { return null; }
        public java.io.InputStream getUnicodeStream(String columnLabel) { return null; }
        public java.io.InputStream getBinaryStream(String columnLabel) { return null; }
        public java.sql.ResultSetMetaData getMetaData() { return null; }
        public java.sql.Array getArray(String columnLabel) { return null; }
        public java.sql.Ref getRef(String columnLabel) { return null; }
        public java.sql.Blob getBlob(String columnLabel) { return null; }
        public java.sql.Clob getClob(String columnLabel) { return null; }
        public java.sql.NClob getNClob(String columnLabel) { return null; }
        public java.sql.SQLXML getSQLXML(String columnLabel) { return null; }
        public String getNString(String columnLabel) { return getString(columnLabel); }
        public java.io.Reader getNCharacterStream(String columnLabel) { return null; }
        public java.io.Reader getCharacterStream(String columnLabel) { return null; }
        public java.math.BigDecimal getBigDecimal(int columnIndex) { return new java.math.BigDecimal(cursor.getString(columnIndex)); }
        public java.sql.Date getDate(int columnIndex) { return java.sql.Date.valueOf(cursor.getString(columnIndex)); }
        public java.sql.Time getTime(int columnIndex) { return java.sql.Time.valueOf(cursor.getString(columnIndex)); }
        public java.sql.Timestamp getTimestamp(int columnIndex) { return java.sql.Timestamp.valueOf(cursor.getString(columnIndex)); }
        public java.io.InputStream getAsciiStream(int columnIndex) { return null; }
        public java.io.InputStream getUnicodeStream(int columnIndex) { return null; }
        public java.io.InputStream getBinaryStream(int columnIndex) { return null; }
        public java.sql.Array getArray(int columnIndex) { return null; }
        public java.sql.Ref getRef(int columnIndex) { return null; }
        public java.sql.Blob getBlob(int columnIndex) { return null; }
        public java.sql.Clob getClob(int columnIndex) { return null; }
        public java.sql.NClob getNClob(int columnIndex) { return null; }
        public java.sql.SQLXML getSQLXML(int columnIndex) { return null; }
        public String getNString(int columnIndex) { return getString(columnIndex); }
        public java.io.Reader getNCharacterStream(int columnIndex) { return null; }
        public java.io.Reader getCharacterStream(int columnIndex) { return null; }
        public boolean getBoolean(int columnIndex) { return cursor.getInt(columnIndex) != 0; }
        public byte getByte(int columnIndex) { return (byte) cursor.getInt(columnIndex); }
        public short getShort(int columnIndex) { return cursor.getShort(columnIndex); }
        public long getLong(int columnIndex) { return cursor.getLong(columnIndex); }
        public float getFloat(int columnIndex) { return cursor.getFloat(columnIndex); }
        public byte[] getBytes(String columnLabel) { return cursor.getBlob(cursor.getColumnIndex(columnLabel)); }
        public byte[] getBytes(int columnIndex) { return cursor.getBlob(columnIndex); }
        public java.sql.Date getDate(String columnLabel, java.util.Calendar cal) { return getDate(columnLabel); }
        public java.sql.Date getDate(int columnIndex, java.util.Calendar cal) { return getDate(columnIndex); }
        public java.sql.Time getTime(String columnLabel, java.util.Calendar cal) { return getTime(columnLabel); }
        public java.sql.Time getTime(int columnIndex, java.util.Calendar cal) { return getTime(columnIndex); }
        public java.sql.Timestamp getTimestamp(String columnLabel, java.util.Calendar cal) { return getTimestamp(columnLabel); }
        public java.sql.Timestamp getTimestamp(int columnIndex, java.util.Calendar cal) { return getTimestamp(columnIndex); }
        public Object getObject(String columnLabel, java.util.Map<String, Class<?>> map) { return null; }
        public Object getObject(int columnIndex, java.util.Map<String, Class<?>> map) { return null; }
        public Object getObject(String columnLabel) { return null; }
        public Object getObject(int columnIndex) { return null; }
        public int findColumn(String columnLabel) { return cursor.getColumnIndex(columnLabel); }
        public java.math.BigDecimal getBigDecimal(String columnLabel, int scale) { return getBigDecimal(columnLabel); }
        public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) { return getBigDecimal(columnIndex); }
        public boolean isBeforeFirst() { return cursor.isBeforeFirst(); }
        public boolean isAfterLast() { return cursor.isAfterLast(); }
        public boolean isFirst() { return cursor.isFirst(); }
        public boolean isLast() { return cursor.isLast(); }
        public void beforeFirst() { cursor.moveToPosition(-1); }
        public void afterLast() { cursor.moveToPosition(cursor.getCount()); }
        public boolean first() { return cursor.moveToFirst(); }
        public boolean last() { return cursor.moveToLast(); }
        public int getRow() { return cursor.getPosition(); }
        public boolean absolute(int row) { return cursor.moveToPosition(row); }
        public boolean relative(int rows) { return cursor.move(rows); }
        public boolean previous() { return cursor.moveToPrevious(); }
        public void setFetchDirection(int direction) { }
        public int getFetchDirection() { return java.sql.ResultSet.FETCH_FORWARD; }
        public void setFetchSize(int rows) { }
        public int getFetchSize() { return 0; }
        public int getType() { return java.sql.ResultSet.TYPE_FORWARD_ONLY; }
        public int getConcurrency() { return java.sql.ResultSet.CONCUR_READ_ONLY; }
        public boolean rowUpdated() { return false; }
        public boolean rowInserted() { return false; }
        public boolean rowDeleted() { return false; }
        
        // Методы обновления - выбрасывают исключение
        public void updateNull(int columnIndex) { throw new UnsupportedOperationException(); }
        public void updateBoolean(int columnIndex, boolean x) { throw new UnsupportedOperationException(); }
        public void updateByte(int columnIndex, byte x) { throw new UnsupportedOperationException(); }
        public void updateShort(int columnIndex, short x) { throw new UnsupportedOperationException(); }
        public void updateInt(int columnIndex, int x) { throw new UnsupportedOperationException(); }
        public void updateLong(int columnIndex, long x) { throw new UnsupportedOperationException(); }
        public void updateFloat(int columnIndex, float x) { throw new UnsupportedOperationException(); }
        public void updateDouble(int columnIndex, double x) { throw new UnsupportedOperationException(); }
        public void updateBigDecimal(int columnIndex, java.math.BigDecimal x) { throw new UnsupportedOperationException(); }
        public void updateString(int columnIndex, String x) { throw new UnsupportedOperationException(); }
        public void updateBytes(int columnIndex, byte[] x) { throw new UnsupportedOperationException(); }
        public void updateDate(int columnIndex, java.sql.Date x) { throw new UnsupportedOperationException(); }
        public void updateTime(int columnIndex, java.sql.Time x) { throw new UnsupportedOperationException(); }
        public void updateTimestamp(int columnIndex, java.sql.Timestamp x) { throw new UnsupportedOperationException(); }
        public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) { throw new UnsupportedOperationException(); }
        public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) { throw new UnsupportedOperationException(); }
        public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) { throw new UnsupportedOperationException(); }
        public void updateObject(int columnIndex, Object x, int scaleOrLength) { throw new UnsupportedOperationException(); }
        public void updateObject(int columnIndex, Object x) { throw new UnsupportedOperationException(); }
        public void updateNull(String columnLabel) { throw new UnsupportedOperationException(); }
        public void updateBoolean(String columnLabel, boolean x) { throw new UnsupportedOperationException(); }
        public void updateByte(String columnLabel, byte x) { throw new UnsupportedOperationException(); }
        public void updateShort(String columnLabel, short x) { throw new UnsupportedOperationException(); }
        public void updateInt(String columnLabel, int x) { throw new UnsupportedOperationException(); }
        public void updateLong(String columnLabel, long x) { throw new UnsupportedOperationException(); }
        public void updateFloat(String columnLabel, float x) { throw new UnsupportedOperationException(); }
        public void updateDouble(String columnLabel, double x) { throw new UnsupportedOperationException(); }
        public void updateBigDecimal(String columnLabel, java.math.BigDecimal x) { throw new UnsupportedOperationException(); }
        public void updateString(String columnLabel, String x) { throw new UnsupportedOperationException(); }
        public void updateBytes(String columnLabel, byte[] x) { throw new UnsupportedOperationException(); }
        public void updateDate(String columnLabel, java.sql.Date x) { throw new UnsupportedOperationException(); }
        public void updateTime(String columnLabel, java.sql.Time x) { throw new UnsupportedOperationException(); }
        public void updateTimestamp(String columnLabel, java.sql.Timestamp x) { throw new UnsupportedOperationException(); }
        public void updateAsciiStream(String columnLabel, java.io.InputStream x, int length) { throw new UnsupportedOperationException(); }
        public void updateBinaryStream(String columnLabel, java.io.InputStream x, int length) { throw new UnsupportedOperationException(); }
        public void updateCharacterStream(String columnLabel, java.io.Reader reader, int length) { throw new UnsupportedOperationException(); }
        public void updateObject(String columnLabel, Object x, int scaleOrLength) { throw new UnsupportedOperationException(); }
        public void updateObject(String columnLabel, Object x) { throw new UnsupportedOperationException(); }
        public void insertRow() { throw new UnsupportedOperationException(); }
        public void updateRow() { throw new UnsupportedOperationException(); }
        public void deleteRow() { throw new UnsupportedOperationException(); }
        public void refreshRow() { throw new UnsupportedOperationException(); }
        public void cancelRowUpdates() { throw new UnsupportedOperationException(); }
        public void moveToInsertRow() { throw new UnsupportedOperationException(); }
        public void moveToCurrentRow() { throw new UnsupportedOperationException(); }
        public java.sql.Statement getStatement() { return null; }
        public void updateRef(int columnIndex, java.sql.Ref x) { throw new UnsupportedOperationException(); }
        public void updateRef(String columnLabel, java.sql.Ref x) { throw new UnsupportedOperationException(); }
        public void updateBlob(int columnIndex, java.sql.Blob x) { throw new UnsupportedOperationException(); }
        public void updateBlob(String columnLabel, java.sql.Blob x) { throw new UnsupportedOperationException(); }
        public void updateClob(int columnIndex, java.sql.Clob x) { throw new UnsupportedOperationException(); }
        public void updateClob(String columnLabel, java.sql.Clob x) { throw new UnsupportedOperationException(); }
        public void updateArray(int columnIndex, java.sql.Array x) { throw new UnsupportedOperationException(); }
        public void updateArray(String columnLabel, java.sql.Array x) { throw new UnsupportedOperationException(); }
        public java.sql.RowId getRowId(int columnIndex) { return null; }
        public java.sql.RowId getRowId(String columnLabel) { return null; }
        public void updateRowId(int columnIndex, java.sql.RowId x) { throw new UnsupportedOperationException(); }
        public void updateRowId(String columnLabel, java.sql.RowId x) { throw new UnsupportedOperationException(); }
        public int getHoldability() { return java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT; }
        public boolean isClosed() { return isClosed; }
        public void updateNString(int columnIndex, String nString) { throw new UnsupportedOperationException(); }
        public void updateNString(String columnLabel, String nString) { throw new UnsupportedOperationException(); }
        public void updateNClob(int columnIndex, java.sql.NClob nClob) { throw new UnsupportedOperationException(); }
        public void updateNClob(String columnLabel, java.sql.NClob nClob) { throw new UnsupportedOperationException(); }
        public void updateSQLXML(int columnIndex, java.sql.SQLXML xmlObject) { throw new UnsupportedOperationException(); }
        public void updateSQLXML(String columnLabel, java.sql.SQLXML xmlObject) { throw new UnsupportedOperationException(); }
        public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) { throw new UnsupportedOperationException(); }
        public void updateNCharacterStream(String columnLabel, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public void updateAsciiStream(int columnIndex, java.io.InputStream x, long length) { throw new UnsupportedOperationException(); }
        public void updateBinaryStream(int columnIndex, java.io.InputStream x, long length) { throw new UnsupportedOperationException(); }
        public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) { throw new UnsupportedOperationException(); }
        public void updateAsciiStream(String columnLabel, java.io.InputStream x, long length) { throw new UnsupportedOperationException(); }
        public void updateBinaryStream(String columnLabel, java.io.InputStream x, long length) { throw new UnsupportedOperationException(); }
        public void updateCharacterStream(String columnLabel, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public void updateBlob(int columnIndex, java.io.InputStream inputStream, long length) { throw new UnsupportedOperationException(); }
        public void updateBlob(String columnLabel, java.io.InputStream inputStream, long length) { throw new UnsupportedOperationException(); }
        public void updateClob(int columnIndex, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public void updateClob(String columnLabel, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public void updateNClob(int columnIndex, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public void updateNClob(String columnLabel, java.io.Reader reader, long length) { throw new UnsupportedOperationException(); }
        public <T> T getObject(int columnIndex, Class<T> type) { return null; }
        public <T> T getObject(String columnLabel, Class<T> type) { return null; }
        public void updateObject(int columnIndex, Object x, Object targetSqlType, int scaleOrLength) { throw new UnsupportedOperationException(); }
        public void updateObject(String columnLabel, Object x, Object targetSqlType, int scaleOrLength) { throw new UnsupportedOperationException(); }
        public void updateObject(int columnIndex, Object x, Object targetSqlType) { throw new UnsupportedOperationException(); }
        public void updateObject(String columnLabel, Object x, Object targetSqlType) { throw new UnsupportedOperationException(); }
    }
} 