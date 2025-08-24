package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для репозиториев
 */
public class RepositoryConstants {
    
    // ========================================
    // НАСТРОЙКИ БАЗЫ ДАННЫХ
    // ========================================
    
    // Путь к базе данных
    public static final String DATABASE_PATH = "budget_master.db";
    
    // ========================================
    // ИМЕНА ТАБЛИЦ
    // ========================================
    
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_CURRENCIES = "currencies";
    public static final String TABLE_OPERATIONS = "operations";
    
    // ========================================
    // СООБЩЕНИЯ ОБ ОШИБКАХ
    // ========================================
    
    public static final String ERROR_DATABASE_CONNECTION = "Ошибка подключения к базе данных";
    public static final String ERROR_SQL_EXECUTION = "Ошибка выполнения SQL-запроса";
    public static final String ERROR_RESULT_SET_READING = "Ошибка чтения данных из ResultSet";
    public static final String ERROR_INVALID_DB_PATH = "Некорректный путь к базе данных";
    public static final String ERROR_TABLE_NOT_FOUND = "Таблица не найдена";
    public static final String ERROR_COLUMN_NOT_FOUND = "Столбец не найден";
    
    // ========================================
    // НАСТРОЙКИ ПОДКЛЮЧЕНИЯ
    // ========================================
    
    public static final int DEFAULT_TIMEOUT = 30; // секунды
    public static final boolean AUTO_COMMIT = true;
    public static final String TRANSACTION_ISOLATION = "READ_COMMITTED";
    
} 