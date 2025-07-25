// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.database;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с базой данных
 * Абстрагирует взаимодействие с различными стандартами БД (JDBC, Android SQLite)
 */
public interface DatabaseConnection {
    
    /**
     * Выполняет SELECT запрос и возвращает список результатов
     * @param sql SQL запрос
     * @param mapper функция для преобразования строки в объект
     * @param params параметры запроса
     * @return список объектов
     */
    <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... params);
    
    /**
     * Выполняет SELECT запрос и возвращает один результат
     * @param sql SQL запрос
     * @param mapper функция для преобразования строки в объект
     * @param params параметры запроса
     * @return Optional с объектом
     */
    <T> Optional<T> executeQuerySingle(String sql, RowMapper<T> mapper, Object... params);
    
    /**
     * Выполняет INSERT/UPDATE/DELETE запрос
     * @param sql SQL запрос
     * @param params параметры запроса
     * @return количество затронутых строк
     */
    int executeUpdate(String sql, Object... params);
    
    /**
     * Выполняет INSERT запрос и возвращает сгенерированный ID
     * @param sql SQL запрос
     * @param params параметры запроса
     * @return сгенерированный ID
     */
    long executeInsert(String sql, Object... params);
    
    /**
     * Получает максимальное значение столбца
     * @param tableName имя таблицы
     * @param columnName имя столбца
     * @param whereCondition условие WHERE (может быть null)
     * @return максимальное значение
     */
    int getMaxValue(String tableName, String columnName, String whereCondition);
    
    /**
     * Начинает транзакцию
     */
    void beginTransaction();
    
    /**
     * Подтверждает транзакцию
     */
    void commit();
    
    /**
     * Откатывает транзакцию
     */
    void rollback();
    
    /**
     * Закрывает соединение
     */
    void close();
    
    /**
     * Проверяет, закрыто ли соединение
     * @return true если соединение закрыто
     */
    boolean isClosed();
    
    /**
     * Интерфейс для преобразования строки результата в объект
     */
    interface RowMapper<T> {
        T mapRow(ResultRow row);
    }
    
    /**
     * Интерфейс для работы со строкой результата
     */
    interface ResultRow {
        Object getObject(String columnName);
        Object getObject(int columnIndex);
        String getString(String columnName);
        String getString(int columnIndex);
        Integer getInt(String columnName);
        Integer getInt(int columnIndex);
        Long getLong(String columnName);
        Long getLong(int columnIndex);
        Boolean getBoolean(String columnName);
        Boolean getBoolean(int columnIndex);
        boolean next();
    }
} 