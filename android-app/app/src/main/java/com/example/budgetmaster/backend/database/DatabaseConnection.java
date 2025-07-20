package com.example.budgetmaster.backend.database;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с базой данных
 */
public interface DatabaseConnection {
    
    /**
     * Функциональный интерфейс для маппинга строк результатов
     */
    @FunctionalInterface
    interface RowMapper<T> {
        T mapRow(ResultRow row);
    }
    
    /**
     * Интерфейс для работы с результатами запроса
     */
    interface ResultRow {
        Integer getInt(String columnName);
        String getString(String columnName);
        Double getDouble(String columnName);
        Boolean getBoolean(String columnName);
        java.time.LocalDateTime getDateTime(String columnName);
        boolean next();
        void close();
    }
    
    /**
     * Выполняет запрос и возвращает список результатов
     */
    <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params);
    
    /**
     * Выполняет запрос и возвращает один результат
     */
    <T> Optional<T> executeQuerySingle(String sql, RowMapper<T> mapper, Object... params);
    
    /**
     * Выполняет INSERT запрос
     */
    long executeInsert(String sql, Object... params);
    
    /**
     * Выполняет UPDATE запрос
     */
    int executeUpdate(String sql, Object... params);
    
    /**
     * Выполняет DELETE запрос
     */
    int executeDelete(String sql, Object... params);
    
    /**
     * Начинает транзакцию
     */
    void beginTransaction();
    
    /**
     * Подтверждает транзакцию
     */
    void commitTransaction();
    
    /**
     * Откатывает транзакцию
     */
    void rollbackTransaction();
    
    /**
     * Закрывает соединение
     */
    void close();
} 