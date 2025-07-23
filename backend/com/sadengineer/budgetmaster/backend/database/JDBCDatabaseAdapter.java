package com.sadengineer.budgetmaster.backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC адаптер для работы с SQLite
 * Основная реализация для backend
 */
public class JDBCDatabaseAdapter implements DatabaseInterface {
    
    private Connection connection;
    private Statement statement;
    
    @Override
    public void connect(String path) {
        try {
            // Загружаем JDBC драйвер для SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Создаем соединение
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            statement = connection.createStatement();
            
            System.out.println("JDBC: Подключение к базе данных успешно: " + path);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC: Ошибка загрузки драйвера SQLite: " + e.getMessage());
            throw new RuntimeException("Не удалось загрузить JDBC драйвер SQLite", e);
        } catch (SQLException e) {
            System.err.println("JDBC: Ошибка подключения к базе данных: " + e.getMessage());
            throw new RuntimeException("Не удалось подключиться к базе данных", e);
        }
    }
    
    @Override
    public void executeSQL(String sql) {
        if (!isConnected()) {
            throw new RuntimeException("Соединение с базой данных не установлено");
        }
        
        try {
            statement.execute(sql);
            System.out.println("JDBC: Выполнен SQL запрос: " + sql);
        } catch (SQLException e) {
            System.err.println("JDBC: Ошибка выполнения SQL запроса: " + e.getMessage());
            throw new RuntimeException("Ошибка выполнения SQL запроса", e);
        }
    }
    
    @Override
    public ResultSet query(String sql) {
        if (!isConnected()) {
            throw new RuntimeException("Соединение с базой данных не установлено");
        }
        
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            System.out.println("JDBC: Выполнен запрос: " + sql);
            return resultSet;
        } catch (SQLException e) {
            System.err.println("JDBC: Ошибка выполнения запроса: " + e.getMessage());
            throw new RuntimeException("Ошибка выполнения запроса", e);
        }
    }
    
    @Override
    public void close() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
            System.out.println("JDBC: Соединение с базой данных закрыто");
        } catch (SQLException e) {
            System.err.println("JDBC: Ошибка закрытия соединения: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
} 