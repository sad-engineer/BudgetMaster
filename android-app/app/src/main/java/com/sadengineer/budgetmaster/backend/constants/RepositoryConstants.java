
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
    
    // JDBC URL
    public static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    
    // Кодировка базы данных
    public static final String DATABASE_ENCODING = "UTF-8";
    public static final String PRAGMA_ENCODING_SQL = "PRAGMA encoding = 'UTF-8'";
    
    // ========================================
    // ИМЕНА ТАБЛИЦ
    // ========================================
    
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_CURRENCIES = "currencies";
    public static final String TABLE_OPERATIONS = "operations";
    
    // ========================================
    // ОБЩИЕ СТОЛБЦЫ (BaseEntity)
    // ========================================
    
    // Основные поля
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TITLE = "title";
    
    // Временные метки
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_UPDATE_TIME = "update_time";
    public static final String COLUMN_DELETE_TIME = "delete_time";
    
    // Пользователи
    public static final String COLUMN_CREATED_BY = "created_by";
    public static final String COLUMN_UPDATED_BY = "updated_by";
    public static final String COLUMN_DELETED_BY = "deleted_by";
    
    // Специфичные столбцы для счетов
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CURRENCY_ID = "currency_id";
    public static final String COLUMN_CLOSED = "closed";
    public static final String COLUMN_CREDIT_CARD_LIMIT = "credit_card_limit";
    public static final String COLUMN_CREDIT_CARD_CATEGORY_ID = "credit_card_category_id";
    public static final String COLUMN_CREDIT_CARD_COMMISSION_CATEGORY_ID = "credit_card_commission_category_id";
    
    // Специфичные столбцы для категорий
    public static final String COLUMN_OPERATION_TYPE = "operation_type";
    public static final String COLUMN_PARENT_ID = "parent_id";
    
    // Специфичные столбцы для операций
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_TO_ACCOUNT_ID = "to_account_id";
    public static final String COLUMN_TO_CURRENCY_ID = "to_currency_id";
    public static final String COLUMN_TO_AMOUNT = "to_amount";
    
    // ========================================
    // СПИСКИ СТОЛБЦОВ ПО СУЩНОСТЯМ (в порядке БД)
    // ========================================
    
    // Столбцы для счетов (accounts)
    public static final String[] ACCOUNT_COLUMNS = {
        COLUMN_ID, 
        COLUMN_TITLE, 
        COLUMN_POSITION, 
        COLUMN_AMOUNT, 
        COLUMN_TYPE, 
        COLUMN_CURRENCY_ID, 
        COLUMN_CLOSED, 
        COLUMN_CREDIT_CARD_LIMIT, 
        COLUMN_CREDIT_CARD_CATEGORY_ID, 
        COLUMN_CREDIT_CARD_COMMISSION_CATEGORY_ID,
        COLUMN_CREATED_BY, 
        COLUMN_UPDATED_BY, 
        COLUMN_DELETED_BY, 
        COLUMN_CREATE_TIME, 
        COLUMN_DELETE_TIME, 
        COLUMN_UPDATE_TIME,
    };

    // Столбцы для бюджетов (budgets)
    public static final String[] BUDGET_COLUMNS = {
        COLUMN_ID, 
        COLUMN_AMOUNT, 
        COLUMN_CURRENCY_ID, 
        COLUMN_CATEGORY_ID, 
        COLUMN_POSITION, 
        COLUMN_CREATED_BY, 
        COLUMN_UPDATED_BY, 
        COLUMN_DELETED_BY, 
        COLUMN_CREATE_TIME, 
        COLUMN_DELETE_TIME, 
        COLUMN_UPDATE_TIME,
    };

    // Столбцы для категорий (categories)
    public static final String[] CATEGORY_COLUMNS = {
        COLUMN_ID, 
        COLUMN_TITLE, 
        COLUMN_POSITION, 
        COLUMN_OPERATION_TYPE, 
        COLUMN_TYPE, 
        COLUMN_PARENT_ID,
        COLUMN_CREATED_BY, 
        COLUMN_UPDATED_BY, 
        COLUMN_DELETED_BY, 
        COLUMN_CREATE_TIME, 
        COLUMN_DELETE_TIME, 
        COLUMN_UPDATE_TIME,
    };
    
    // Столбцы для валют (currencies)
    public static final String[] CURRENCY_COLUMNS = {
        COLUMN_ID, 
        COLUMN_TITLE, 
        COLUMN_POSITION, 
        COLUMN_CREATED_BY, 
        COLUMN_UPDATED_BY, 
        COLUMN_DELETED_BY,
        COLUMN_CREATE_TIME, 
        COLUMN_DELETE_TIME, 
        COLUMN_UPDATE_TIME,
    };
    
    // Столбцы для операций (operations)
    public static final String[] OPERATION_COLUMNS = {
        COLUMN_ID, 
        COLUMN_TYPE, 
        COLUMN_DATE, 
        COLUMN_AMOUNT, 
        COLUMN_COMMENT, 
        COLUMN_CATEGORY_ID, 
        COLUMN_ACCOUNT_ID, 
        COLUMN_CURRENCY_ID,
        COLUMN_TO_ACCOUNT_ID, 
        COLUMN_TO_CURRENCY_ID, 
        COLUMN_TO_AMOUNT, 
        COLUMN_CREATED_BY, 
        COLUMN_UPDATED_BY, 
        COLUMN_DELETED_BY,
        COLUMN_CREATE_TIME, 
        COLUMN_DELETE_TIME, 
        COLUMN_UPDATE_TIME,
    };

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