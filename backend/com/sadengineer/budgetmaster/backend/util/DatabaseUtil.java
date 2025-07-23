// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.sadengineer.budgetmaster.backend.model.Currency;
import com.sadengineer.budgetmaster.backend.model.Category;
import com.sadengineer.budgetmaster.backend.model.Account;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;
import com.sadengineer.budgetmaster.backend.database.DatabaseProvider;
import com.sadengineer.budgetmaster.backend.database.DatabaseInterface;
import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.ACCOUNT_TYPE_CURRENT;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.DEFAULT_CURRENCY_ID;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.ACCOUNT_STATUS_OPEN;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.ACCOUNT_TYPE_SAVINGS;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.ACCOUNT_TYPE_CREDIT;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.OPERATION_TYPE_INCOME;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.OPERATION_TYPE_EXPENSE;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.CATEGORY_TYPE_PARENT;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.CATEGORY_TYPE_CHILD;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.ACCOUNT_STATUS_OPEN;

/**
 * Класс для работы с базой данных
 * 
 * <p>Содержит методы для работы с базой данных:
 * <ul>
 *   <li>Очистка всех данных из всех таблиц</li>
 *   <li>Очистка указанной таблицы</li>
 *   <li>Создание базы данных, если она не существует</li>
 *   <li>Получение количества записей в указанной таблице</li>
 *   <li>Получение общего количества записей во всех таблицах</li>
 *   <li>Восстановление дефолтных категорий</li>
 *   <li>Восстановление дефолтных валют</li>
 *   <li>Восстановление дефолтных значений во всей базе</li>
 * </ul>
 * 
 * <p>Все методы используют UTF-8 кодировку для корректной работы с кириллицей.
 */
public class DatabaseUtil {
    /**
     * Очищает все данные из всех таблиц
     * @param dbPath путь к файлу базы данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void clearAllData(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                // Временно отключаем внешние ключи
                stmt.execute("PRAGMA foreign_keys = OFF");
                // Очищаем все таблицы в обратном порядке зависимостей
                stmt.execute("DELETE FROM " + TABLE_OPERATIONS);
                stmt.execute("DELETE FROM " + TABLE_BUDGETS);
                stmt.execute("DELETE FROM " + TABLE_ACCOUNTS);
                stmt.execute("DELETE FROM " + TABLE_CATEGORIES);
                stmt.execute("DELETE FROM " + TABLE_CURRENCIES);
                // Сбрасываем счетчики автоинкремента
                stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('" + TABLE_OPERATIONS + "', '" + TABLE_BUDGETS + "', '" + TABLE_ACCOUNTS + "', '" + TABLE_CATEGORIES + "', '" + TABLE_CURRENCIES + "')");
                // Включаем внешние ключи обратно
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
    }
    /**
     * Очищает данные из указанной таблицы
     * @param dbPath путь к файлу базы данных
     * @param tableName имя таблицы
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void clearTable(String dbPath, String tableName) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                // Временно отключаем внешние ключи
                stmt.execute("PRAGMA foreign_keys = OFF");
                // Очищаем таблицу
                stmt.execute("DELETE FROM " + tableName);
                // Сбрасываем счетчик автоинкремента
                stmt.execute("DELETE FROM sqlite_sequence WHERE name = '" + tableName + "'");
                // Включаем внешние ключи обратно
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    /**
     * Создает базу данных, если она не существует
     * @param dbPath путь к файлу базы данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void createDatabaseIfNotExists(String dbPath) throws SQLException {
        DatabaseInterface db = DatabaseProvider.getDatabase();
        db.connect(dbPath);
        
        try {
            // Устанавливаем кодировку UTF-8 для базы
            db.executeSQL("PRAGMA encoding = 'UTF-8'");
            db.executeSQL("PRAGMA foreign_keys = ON");
            db.executeSQL("PRAGMA case_sensitive_like = OFF");
            
            // Создаем таблицы
            createTables(db);
            // Инициализируем дефолтные валюты
            initializeDefaultCurrencies(db);
            // Инициализируем дефолтные категории
            initializeDefaultCategories(db);
            // Инициализируем дефолтные счета
            initializeDefaultAccounts(db);
        } finally {
            db.close();
        }
    }

    /**
     * Получает количество записей в указанной таблице
     * @param dbPath путь к файлу базы данных
     * @param tableName имя таблицы
     * @return количество записей
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static int getTableRecordCount(String dbPath, String tableName) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Получает общее количество записей во всех таблицах
     * @param dbPath путь к файлу базы данных
     * @return общее количество записей
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static int getTotalRecordCount(String dbPath) throws SQLException {
        String[] tables = {TABLE_CURRENCIES, TABLE_CATEGORIES, TABLE_ACCOUNTS, TABLE_BUDGETS, TABLE_OPERATIONS};
        int total = 0;
        for (String table : tables) {
            total += getTableRecordCount(dbPath, table);
        }
        return total;
    }

    /**
     * Восстанавливает только дефолтные категории
     * @param dbPath путь к файлу базы данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void restoreDefaultCategories(String dbPath) throws SQLException {
        // Очищаем таблицу категорий
        clearTable(dbPath, TABLE_CATEGORIES);
        // Переинициализируем категории
        DatabaseInterface db = DatabaseProvider.getDatabase();
        db.connect(dbPath);
        try {
            initializeDefaultCategories(db);
        } finally {
            db.close();
        }
    }

    /**
     * Восстанавливает только дефолтные валюты
     * @param dbPath путь к файлу базы данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void restoreDefaultCurrencies(String dbPath) throws SQLException {
        // Очищаем таблицу валют
        clearTable(dbPath, TABLE_CURRENCIES);
        // Переинициализируем валюты
        DatabaseInterface db = DatabaseProvider.getDatabase();
        db.connect(dbPath);
        try {
            initializeDefaultCurrencies(db);
        } finally {
            db.close();
        }
    }
    
    /**
     * Восстанавливает дефолтные значения во всей базе
     * @param dbPath путь к файлу базы данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    public static void restoreDefaults(String dbPath) throws SQLException {
        // Сначала очищаем все данные
        clearAllData(dbPath);
        // Переинициализируем дефолтные значения
        DatabaseInterface db = DatabaseProvider.getDatabase();
        db.connect(dbPath);
        try {
            initializeDefaultCurrencies(db);
            initializeDefaultCategories(db);
            initializeDefaultAccounts(db);
        } finally {
            db.close();
        }
    }

    /**
     * Создает таблицы в базе данных, если они не существуют
     * @param db интерфейс для работы с базой данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    private static void createTables(DatabaseInterface db) throws SQLException {
            // Таблица валют
        db.executeSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_CURRENCIES + " (" + 
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_POSITION + " INTEGER NOT NULL, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_DELETED_BY + " TEXT, " +
                COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL, " +
                COLUMN_UPDATE_TIME + " TIMESTAMP, " +
                COLUMN_DELETE_TIME + " TIMESTAMP" +
                ")");
            // Таблица счетов
        db.executeSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_ACCOUNTS + " (" + 
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_POSITION + " INTEGER NOT NULL, " +
                COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                COLUMN_TYPE + " INTEGER NOT NULL, " +
                COLUMN_CURRENCY_ID + " INTEGER NOT NULL, " +
                COLUMN_CLOSED + " INTEGER NOT NULL DEFAULT " + ACCOUNT_STATUS_OPEN + ", " +
                COLUMN_CREDIT_CARD_LIMIT + " INTEGER, " +
                COLUMN_CREDIT_CARD_CATEGORY_ID + " INTEGER, " +
                COLUMN_CREDIT_CARD_COMMISSION_CATEGORY_ID + " INTEGER, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_DELETED_BY + " TEXT, " +
                COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL, " +
                COLUMN_UPDATE_TIME + " TIMESTAMP, " +
                COLUMN_DELETE_TIME + " TIMESTAMP, " +
                "FOREIGN KEY (" + COLUMN_CURRENCY_ID + ") REFERENCES " + TABLE_CURRENCIES + " (" + COLUMN_ID + ")" +
                ")");
            // Таблица категорий
        db.executeSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" + 
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_POSITION + " INTEGER NOT NULL, " +
                COLUMN_OPERATION_TYPE + " INTEGER NOT NULL, " +
                COLUMN_TYPE + " INTEGER NOT NULL, " +
                COLUMN_PARENT_ID + " INTEGER, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_DELETED_BY + " TEXT, " +
                COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL, " +
                COLUMN_UPDATE_TIME + " TIMESTAMP, " +
                COLUMN_DELETE_TIME + " TIMESTAMP, " +
                "FOREIGN KEY (" + COLUMN_PARENT_ID + ") REFERENCES " + TABLE_CATEGORIES + " (" + COLUMN_ID + ")" +
                ")");
            // Таблица бюджетов
        db.executeSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGETS + " (" + 
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                COLUMN_CURRENCY_ID + " INTEGER NOT NULL, " +
                COLUMN_CATEGORY_ID + " INTEGER, " +
                COLUMN_POSITION + " INTEGER NOT NULL, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_DELETED_BY + " TEXT, " +
                COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL, " +
                COLUMN_UPDATE_TIME + " TIMESTAMP, " +
                COLUMN_DELETE_TIME + " TIMESTAMP, " +
                "FOREIGN KEY (" + COLUMN_CURRENCY_ID + ") REFERENCES " + TABLE_CURRENCIES + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + " (" + COLUMN_ID + ")" +
                ")");
            // Таблица операций
        db.executeSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_OPERATIONS + " (" + 
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " INTEGER NOT NULL, " +
                COLUMN_DATE + " TIMESTAMP NOT NULL, " +
                COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                COLUMN_COMMENT + " TEXT NOT NULL, " +
                COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                COLUMN_ACCOUNT_ID + " INTEGER NOT NULL, " +
                COLUMN_CURRENCY_ID + " INTEGER NOT NULL, " +
                COLUMN_TO_ACCOUNT_ID + " INTEGER, " +
                COLUMN_TO_CURRENCY_ID + " INTEGER, " +
                COLUMN_TO_AMOUNT + " INTEGER, " +
                COLUMN_CREATED_BY + " TEXT, " +
                COLUMN_UPDATED_BY + " TEXT, " +
                COLUMN_DELETED_BY + " TEXT, " +
                COLUMN_CREATE_TIME + " TIMESTAMP NOT NULL, " +
                COLUMN_UPDATE_TIME + " TIMESTAMP, " +
                COLUMN_DELETE_TIME + " TIMESTAMP, " +
                "FOREIGN KEY (" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_CURRENCY_ID + ") REFERENCES " + TABLE_CURRENCIES + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_TO_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_TO_CURRENCY_ID + ") REFERENCES " + TABLE_CURRENCIES + " (" + COLUMN_ID + ")" +
                ")");
        }
    
    /**
     * Инициализирует дефолтные категории
     * @param db интерфейс для работы с базой данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    private static void initializeDefaultCategories(DatabaseInterface db) throws SQLException {
        // Check if categories already exist in table
        ResultSet rs = db.query("SELECT COUNT(*) FROM " + TABLE_CATEGORIES);
        if (rs.next() && rs.getInt(1) > 0) {
            // Categories already exist, don't add
            rs.close();
            return;
        }
        rs.close();
        
        // Создаем родительские категории
        db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Доходы', 1, " + OPERATION_TYPE_INCOME + ", " + CATEGORY_TYPE_PARENT + ", NULL, 'initializer', datetime('now'))");
        db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Расходы', 2, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_PARENT + ", NULL, 'initializer', datetime('now'))");
        
        // Получаем ID созданных родительских категорий
        // Используем более надежный подход - получаем все категории и ищем по позиции
        ResultSet rsAllCategories = db.query("SELECT id, title, position FROM " + TABLE_CATEGORIES + " WHERE position IN (1, 2) ORDER BY position");
        
        int incomeParentId = 0, expenseParentId = 0;
        while (rsAllCategories.next()) {
            int id = rsAllCategories.getInt(1);
            String title = rsAllCategories.getString(2);
            int position = rsAllCategories.getInt(3);
            
            if (position == 1) {
                incomeParentId = id;
            } else if (position == 2) {
                expenseParentId = id;
            }
        }
        rsAllCategories.close();
        
        // Проверяем, что ID получены корректно
        if (incomeParentId == 0 || expenseParentId == 0) {
            throw new SQLException("Не удалось получить ID родительских категорий");
        }
        
        // Создаем дочерние категории доходов
        String[] incomeCategoryTitles = {"Работа", "Подработка", "Подарки"};
        int incomePosition = 3;
        for (String title : incomeCategoryTitles) {
            db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + incomePosition + ", " + OPERATION_TYPE_INCOME + ", " + CATEGORY_TYPE_CHILD + ", " + incomeParentId + ", 'initializer', datetime('now'))");
            incomePosition++;
        }

        // Создаем промежуточные категории расходов
        db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Необходимые', 6, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + expenseParentId + ", 'initializer', datetime('now'))");
        db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Дополнительные', 7, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + expenseParentId + ", 'initializer', datetime('now'))");
        
        // Получаем ID созданных промежуточных категорий
        // Используем поиск по позиции для надежности
        ResultSet rsIntermediateCategories = db.query("SELECT id, title, position FROM " + TABLE_CATEGORIES + " WHERE position IN (6, 7) ORDER BY position");
        
        int necessaryId = 0, additionalId = 0;
        while (rsIntermediateCategories.next()) {
            int id = rsIntermediateCategories.getInt(1);
            String title = rsIntermediateCategories.getString(2);
            int position = rsIntermediateCategories.getInt(3);
            
            if (position == 6) {
                necessaryId = id;
            } else if (position == 7) {
                additionalId = id;
            }
        }
        rsIntermediateCategories.close();
        
        // Проверяем, что ID получены корректно
        if (necessaryId == 0 || additionalId == 0) {
            throw new SQLException("Не удалось получить ID промежуточных категорий");
        }
        
        // Создаем дочерние категории необходимых расходов
        String[] necessaryCategoryTitles = {"Коммунальные", "Продукты", "Транспорт", "Медицина", "Одежда", "Налоги"};
        int necessaryPosition = 8;
        for (String title : necessaryCategoryTitles) {
            db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + necessaryPosition + ", " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + necessaryId + ", 'initializer', datetime('now'))");
            necessaryPosition++;
        }
        
        // Создаем дочерние категории дополнительных расходов
        String[] additionalCategoryTitles = {"Домашние нужды", "Кино", "Кафе и рестораны", "Подарки"};
        int additionalPosition = 14;
        for (String title : additionalCategoryTitles) {
            db.executeSQL("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + additionalPosition + ", " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + additionalId + ", 'initializer', datetime('now'))");
            additionalPosition++;
        }
    }
    
    /**
     * Инициализирует дефолтные валюты
     * @param db интерфейс для работы с базой данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    private static void initializeDefaultCurrencies(DatabaseInterface db) throws SQLException {
        // Check if currencies already exist in table
        ResultSet rs = db.query("SELECT COUNT(*) FROM " + TABLE_CURRENCIES);
            if (rs.next() && rs.getInt(1) > 0) {
                // Currencies already exist, don't add
            rs.close();
                return;
            }
        rs.close();
        
        // Add default currencies through direct SQL
        Object[][] currencyData = {
            {"RUB", 1},
            {"USD", 2},
            {"EUR", 3}
        };
        for (Object[] currencyInfo : currencyData) {
            db.executeSQL("INSERT INTO " + TABLE_CURRENCIES + " (title, position, created_by, create_time) VALUES ('" + currencyInfo[0] + "', " + currencyInfo[1] + ", 'initializer', datetime('now'))");
        }    
    }

    /**
     * Инициализирует дефолтные счета
     * @param db интерфейс для работы с базой данных
     * @throws SQLException если операция с базой завершилась ошибкой
     */
    private static void initializeDefaultAccounts(DatabaseInterface db) throws SQLException {
        // Check if accounts already exist in table
        ResultSet rs = db.query("SELECT COUNT(*) FROM " + TABLE_ACCOUNTS);
            if (rs.next() && rs.getInt(1) > 0) {
                // Accounts already exist, don't add
            rs.close();
                return;
            }
        rs.close();
        // Add default accounts through direct SQL
        Object[][] defaultAccounts = {
            {"Наличные", ACCOUNT_TYPE_CURRENT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 1},
            {"Зарплатная карта", ACCOUNT_TYPE_CURRENT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 2},
            {"Сберегательный счет", ACCOUNT_TYPE_SAVINGS, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 3},
            {"Кредитная карта", ACCOUNT_TYPE_CREDIT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 4},
            {"Карта рассрочки", ACCOUNT_TYPE_CREDIT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 5},
        };
        for (Object[] acc : defaultAccounts) {
            db.executeSQL("INSERT INTO " + TABLE_ACCOUNTS + " (title, amount, type, currency_id, closed, position, created_by, create_time) VALUES ('" + acc[0] + "', 0, " + acc[1] + ", " + acc[2] + ", " + acc[3] + ", " + acc[4] + ", 'initializer', datetime('now'))");
        }
    }
} 