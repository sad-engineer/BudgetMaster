// -*- coding: utf-8 -*-
package database.jdbc;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC реализация DatabaseConnection
 */
public class JdbcDatabaseConnection implements DatabaseConnection {
    
    private final Connection connection;
    private final String url;
    
    public JdbcDatabaseConnection(String dbPath) throws SQLException {
        this.url = "jdbc:sqlite:" + dbPath;
        this.connection = DriverManager.getConnection(url);
        
        // Устанавливаем UTF-8 кодировку
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA encoding = 'UTF-8'");
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }
    
    @Override
    public <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T result = mapper.mapRow(new JdbcResultRow(rs));
                    if (result != null) {
                        results.add(result);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    @Override
    public <T> Optional<T> executeQuerySingle(String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T result = mapper.mapRow(new JdbcResultRow(rs));
                    return Optional.ofNullable(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    @Override
    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public long executeInsert(String sql, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    @Override
    public int getMaxValue(String tableName, String columnName, String whereCondition) {
        String sql = "SELECT MAX(" + columnName + ") FROM " + tableName;
        if (whereCondition != null && !whereCondition.trim().isEmpty()) {
            sql += " WHERE " + whereCondition;
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    @Override
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean isClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
    
    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    /**
     * JDBC реализация ResultRow
     */
    private static class JdbcResultRow implements ResultRow {
        private final ResultSet rs;
        
        public JdbcResultRow(ResultSet rs) {
            this.rs = rs;
        }
        
        @Override
        public Object getObject(String columnName) {
            try {
                return rs.getObject(columnName);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Object getObject(int columnIndex) {
            try {
                return rs.getObject(columnIndex);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public String getString(String columnName) {
            try {
                return rs.getString(columnName);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public String getString(int columnIndex) {
            try {
                return rs.getString(columnIndex);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Integer getInt(String columnName) {
            try {
                return rs.getInt(columnName);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Integer getInt(int columnIndex) {
            try {
                return rs.getInt(columnIndex);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Long getLong(String columnName) {
            try {
                return rs.getLong(columnName);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Long getLong(int columnIndex) {
            try {
                return rs.getLong(columnIndex);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Boolean getBoolean(String columnName) {
            try {
                return rs.getBoolean(columnName);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public Boolean getBoolean(int columnIndex) {
            try {
                return rs.getBoolean(columnIndex);
            } catch (SQLException e) {
                return null;
            }
        }
        
        @Override
        public boolean next() {
            try {
                return rs.next();
            } catch (SQLException e) {
                return false;
            }
        }
    }
} 