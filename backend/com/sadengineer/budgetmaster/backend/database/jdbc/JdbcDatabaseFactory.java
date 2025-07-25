// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.database.jdbc;

import com.sadengineer.budgetmaster.backend.database.DatabaseConnection;
import com.sadengineer.budgetmaster.backend.database.DatabaseFactory;
import java.sql.SQLException;

/**
 * JDBC фабрика для создания соединений с базой данных
 */
public class JdbcDatabaseFactory implements DatabaseFactory.DatabaseProvider {
    
    /**
     * Создает JDBC соединение с базой данных
     * @param dbPath путь к файлу БД
     * @return JDBC соединение
     */
    @Override
    public DatabaseConnection createConnection(String dbPath) {
        try {
            return new JdbcDatabaseConnection(dbPath);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания JDBC соединения: " + e.getMessage(), e);
        }
    }
    
    /**
     * Инициализирует JDBC как провайдер по умолчанию
     */
    public static void initialize() {
        DatabaseFactory.setProvider(new JdbcDatabaseFactory());
    }
} 