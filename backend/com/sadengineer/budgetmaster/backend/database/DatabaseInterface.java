package com.sadengineer.budgetmaster.backend.database;

import java.sql.ResultSet;

/**
 * Интерфейс для работы с базой данных
 * Позволяет инкапсулировать разные реализации SQLite (JDBC, Android SQLite, etc.)
 */
public interface DatabaseInterface {
    
    /**
     * Подключение к базе данных
     * @param path путь к файлу базы данных
     */
    void connect(String path);
    
    /**
     * Выполнение SQL запроса без возврата результата
     * @param sql SQL запрос
     */
    void executeSQL(String sql);
    
    /**
     * Выполнение SQL запроса с возвратом результата
     * @param sql SQL запрос
     * @return результат запроса
     */
    ResultSet query(String sql);
    
    /**
     * Закрытие соединения с базой данных
     */
    void close();
    
    /**
     * Проверка, открыто ли соединение
     * @return true если соединение открыто
     */
    boolean isConnected();
} 