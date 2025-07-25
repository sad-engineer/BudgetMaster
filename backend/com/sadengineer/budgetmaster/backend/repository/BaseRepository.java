// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import com.sadengineer.budgetmaster.backend.database.DatabaseConnection;
import com.sadengineer.budgetmaster.backend.database.DatabaseFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import com.sadengineer.budgetmaster.backend.util.DateTimeUtil;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;

/**
 * Базовый класс для репозиториев с техническими методами работы с базой данных
 * 
 * <p>Содержит общие методы для всех репозиториев:
 * <ul>
 *   <li>Подключение к базе данных через DatabaseConnection</li>
 *   <li>Универсальные CRUD операции (findAll, findByColumn, softDelete)</li>
 *   <li>Безопасная работа с соединениями</li>
 * </ul>
 * 
 * <p>Все методы используют UTF-8 кодировку для корректной работы с кириллицей.
 */
public abstract class BaseRepository {
    protected final String dbPath;
    protected final DatabaseConnection connection;

    /**
     * Конструктор базового репозитория
     * 
     * <p>Инициализирует подключение к базе данных через DatabaseFactory.
     * 
     * @param dbPath путь к файлу базы данных SQLite (например: "budget_master.db")
     * @throws IllegalArgumentException если dbPath равен null или пустой строке
     */
    public BaseRepository(String dbPath) {
        this.dbPath = dbPath;
        this.connection = DatabaseFactory.createConnection(dbPath);
    }

    /**
     * Универсальный метод для поиска всех сущностей
     * 
     * <p>Выполняет SQL-запрос для получения всех записей из указанной таблицы.
     * Результаты преобразуются в объекты с помощью переданной функции маппинга.
     * Возвращает полный список всех записей без фильтрации по статусу удаления.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param mapper функция для маппинга ResultRow в сущность (не null)
     * @return список всех сущностей (может быть пустым, но не null)
     * @throws IllegalArgumentException если tableName равен null/пустой строке
     */
    protected <T> List<T> findAll(String tableName, DatabaseConnection.RowMapper<T> mapper) {
        String sql = "SELECT * FROM " + tableName;
        return connection.executeQuery(sql, mapper);
    }

    /**
     * Универсальный метод для поиска всех сущностей с опциональным условием
     * 
     * <p>Выполняет SQL-запрос для получения записей из указанной таблицы.
     * Если указаны columnName и value, добавляет условие WHERE для фильтрации.
     * Если columnName или value равны null, возвращает все записи без фильтрации.
     * Результаты преобразуются в объекты с помощью переданной функции маппинга.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param columnName имя столбца для фильтрации (может быть null для получения всех записей)
     * @param value значение в столбце для фильтрации (может быть null для получения всех записей)
     * @param mapper функция для маппинга ResultRow в сущность (не null)
     * @return список всех сущностей (может быть пустым, но не null)
     * @throws IllegalArgumentException если tableName равен null/пустой строке
     */
    protected <T> List<T> findAll(String tableName, String columnName, Object value, DatabaseConnection.RowMapper<T> mapper) {
        String sql;
        Object[] params;
        
        if (columnName != null && value != null) {
            sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
            params = new Object[]{value};
        } else {
            sql = "SELECT * FROM " + tableName;
            params = new Object[0];
            }
            
        return connection.executeQuery(sql, mapper, params);
    }

    /**
     * Универсальный метод для поиска сущности по столбцу и значению
     * 
     * <p>Выполняет SQL-запрос для поиска записи по указанному столбцу и значению.
     * Результат преобразуется в объект с помощью переданной функции маппинга.
     * 
     * @param tableName имя таблицы для поиска (не null, не пустая строка)
     * @param columnName имя столбца для поиска (не null, не пустая строка)
     * @param value значение в столбце для поиска (не null)
     * @param mapper функция для маппинга ResultRow в сущность (не null)
     * @return Optional с найденной сущностью, если найдена, иначе пустой Optional
     * @throws IllegalArgumentException если tableName или columnName равны null/пустой строке, или value равен null
     */
    protected <T> Optional<T> findByColumn(String tableName, String columnName, Object value, DatabaseConnection.RowMapper<T> mapper) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ?";
        return connection.executeQuerySingle(sql, mapper, value);
    }

    /**
     * Получение пути к базе данных
     * 
     * @return путь к файлу базы данных SQLite
     */
    public String getDbPath() {
        return dbPath;
    }
    
    /**
     * Получение максимального значения столбца с условием
     * 
     * <p>Выполняет SQL-запрос для получения максимального значения указанного столбца
     * с опциональным условием WHERE. Не загружает все записи, только одно значение.
     * 
     * @param tableName имя таблицы (не null, не пустая строка)
     * @param columnName имя столбца для поиска максимума (не null, не пустая строка)
     * @param whereCondition условие WHERE (если не нужно - пустая строка)
     * @return максимальное значение столбца, 0 если записей нет или произошла ошибка
     */
    protected int getMaxValue(String tableName, String columnName, String whereCondition) {
        return connection.getMaxValue(tableName, columnName, whereCondition);
    }

    /**
     * Мягкое удаление записи по ID
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param tableName имя таблицы (не null, не пустая строка)
     * @param id ID записи для удаления (положительное целое число)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если запись не найдена
     */
    protected boolean softDelete(String tableName, Integer id, String deletedBy) {
        String sql = "UPDATE " + tableName + " SET " + COLUMN_DELETE_TIME + " = ?, " + COLUMN_DELETED_BY + " = ? WHERE " + COLUMN_ID + " = ?";
        String deleteTime = DateTimeUtil.formatForSqlite(DateTimeUtil.getCurrentDateTime());
        int rowsAffected = connection.executeUpdate(sql, deleteTime, deletedBy, id);
        return rowsAffected > 0;
    }

    /**
     * Мягкое удаление записи по столбцу и значению
     * 
     * <p>Устанавливает поля delete_time = текущее время и deleted_by = указанный пользователь.
     * Запись физически не удаляется из базы данных.
     * 
     * @param tableName имя таблицы (не null, не пустая строка)
     * @param columnName имя столбца для поиска (не null, не пустая строка)
     * @param value значение в столбце для поиска (не null)
     * @param deletedBy пользователь, который выполняет удаление (не null, не пустая строка)
     * @return true, если удаление выполнено успешно, false если запись не найдена
     */
    protected boolean softDelete(String tableName, String columnName, Object value, String deletedBy) {
        String sql = "UPDATE " + tableName + " SET " + COLUMN_DELETE_TIME + " = ?, " + COLUMN_DELETED_BY + " = ? WHERE " + columnName + " = ?";
        String deleteTime = DateTimeUtil.formatForSqlite(DateTimeUtil.getCurrentDateTime());
        int rowsAffected = connection.executeUpdate(sql, deleteTime, deletedBy, value);
        return rowsAffected > 0;
    }
    
    /**
     * Закрывает соединение с базой данных
     */
    public void close() {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    /**
     * Получение соединения с базой данных (для обратной совместимости)
     * @deprecated Используйте DatabaseConnection напрямую
     */
    @Deprecated
    protected com.sadengineer.budgetmaster.backend.database.DatabaseConnection getConnection() {
        return connection;
    }  
} 