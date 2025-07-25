// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.database;

/**
 * Фабрика для создания соединений с базой данных
 * Позволяет создавать соединения для разных стандартов БД
 */
public class DatabaseFactory {
    
    private static DatabaseProvider provider;
    
    /**
     * Устанавливает провайдер для создания соединений
     * @param databaseProvider провайдер БД
     */
    public static void setProvider(DatabaseProvider databaseProvider) {
        provider = databaseProvider;
    }
    
    /**
     * Создает соединение с базой данных
     * @param dbPath путь к файлу БД
     * @return соединение с БД
     */
    public static DatabaseConnection createConnection(String dbPath) {
        if (provider == null) {
            throw new IllegalStateException("DatabaseProvider не установлен. Вызовите setProvider() перед созданием соединения.");
        }
        return provider.createConnection(dbPath);
    }
    
    /**
     * Интерфейс провайдера для создания соединений
     */
    public interface DatabaseProvider {
        DatabaseConnection createConnection(String dbPath);
    }
} 