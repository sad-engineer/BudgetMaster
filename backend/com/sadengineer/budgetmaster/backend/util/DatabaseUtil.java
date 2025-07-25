// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.util;

import com.sadengineer.budgetmaster.backend.database.DatabaseConnection;
import com.sadengineer.budgetmaster.backend.database.DatabaseFactory;
import java.util.*;

import static com.sadengineer.budgetmaster.backend.constants.RepositoryConstants.*;
import static com.sadengineer.budgetmaster.backend.constants.ModelConstants.*;

/**
 * Утилиты для работы с базой данных
 * 
 * <p>Содержит методы для:
 * <ul>
 *   <li>Очистки данных из таблиц</li>
 *   <li>Создания базы данных и таблиц</li>
 *   <li>Инициализации дефолтных данных</li>
 *   <li>Получения статистики по базе данных</li>
 * </ul>
 * 
 * <p>Все методы используют новый DatabaseConnection API,
 * что обеспечивает совместимость с Android и JDBC платформами.
 */
public class DatabaseUtil {

    /**
     * Очищает все данные из всех таблиц базы данных
     * @param dbPath путь к файлу базы данных
     */
    public static void clearAllData(String dbPath) {
        String[] tables = {TABLE_OPERATIONS, TABLE_BUDGETS, TABLE_CATEGORIES, TABLE_ACCOUNTS, TABLE_CURRENCIES};
        for (String table : tables) {
            clearTable(dbPath, table);
        }
    }

    /**
     * Очищает данные из указанной таблицы
     * @param dbPath путь к файлу базы данных
     * @param tableName имя таблицы
     */
    public static void clearTable(String dbPath, String tableName) {
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            // Временно отключаем внешние ключи
            connection.executeUpdate("PRAGMA foreign_keys = OFF");
            // Очищаем таблицу
            connection.executeUpdate("DELETE FROM " + tableName);
            // Сбрасываем счетчик автоинкремента
            connection.executeUpdate("DELETE FROM sqlite_sequence WHERE name = '" + tableName + "'");
            // Включаем внешние ключи обратно
            connection.executeUpdate("PRAGMA foreign_keys = ON");
        } finally {
            connection.close();
        }
    }

    /**
     * Создает базу данных, если она не существует
     * @param dbPath путь к файлу базы данных
     */
    public static void createDatabaseIfNotExists(String dbPath) {
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            // Устанавливаем кодировку UTF-8 для базы
            connection.executeUpdate("PRAGMA encoding = 'UTF-8'");
            connection.executeUpdate("PRAGMA foreign_keys = ON");
            connection.executeUpdate("PRAGMA case_sensitive_like = OFF");
            
            // Создаем таблицы
            createTables(connection);
            // Инициализируем дефолтные валюты
            initializeDefaultCurrencies(connection);
            // Инициализируем дефолтные категории
            initializeDefaultCategories(connection);
            // Инициализируем дефолтные счета
            initializeDefaultAccounts(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * Получает количество записей в указанной таблице
     * @param dbPath путь к файлу базы данных
     * @param tableName имя таблицы
     * @return количество записей
     */
    public static int getTableRecordCount(String dbPath, String tableName) {
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;
            return connection.executeQuerySingle(sql, row -> row.getInt(1)).orElse(0);
        } finally {
            connection.close();
        }
    }

    /**
     * Получает общее количество записей во всех таблицах
     * @param dbPath путь к файлу базы данных
     * @return общее количество записей
     */
    public static int getTotalRecordCount(String dbPath) {
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
     */
    public static void restoreDefaultCategories(String dbPath) {
        // Очищаем таблицу категорий
        clearTable(dbPath, TABLE_CATEGORIES);
        // Переинициализируем категории
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            initializeDefaultCategories(connection);
        } finally {
            connection.close();
        }
    }

    /**
     * Восстанавливает только дефолтные валюты
     * @param dbPath путь к файлу базы данных
     */
    public static void restoreDefaultCurrencies(String dbPath) {
        // Очищаем таблицу валют
        clearTable(dbPath, TABLE_CURRENCIES);
        // Переинициализируем валюты
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            initializeDefaultCurrencies(connection);
        } finally {
            connection.close();
        }
    }
    
    /**
     * Восстанавливает дефолтные значения во всей базе
     * @param dbPath путь к файлу базы данных
     */
    public static void restoreDefaults(String dbPath) {
        System.out.println("🔄 restoreDefaults: Начинаем восстановление дефолтных данных");
        
        // Сначала очищаем все данные
        clearAllData(dbPath);
        System.out.println("🔄 restoreDefaults: Данные очищены");
        
        // Переинициализируем дефолтные значения
        DatabaseConnection connection = DatabaseFactory.createConnection(dbPath);
        try {
            initializeDefaultCurrencies(connection);
            System.out.println("🔄 restoreDefaults: Валюты инициализированы");
            
            initializeDefaultCategories(connection);
            System.out.println("🔄 restoreDefaults: Категории инициализированы");
            
            initializeDefaultAccounts(connection);
            System.out.println("🔄 restoreDefaults: Счета инициализированы");
        } finally {
            connection.close();
        }
        
        System.out.println("🔄 restoreDefaults: Восстановление завершено");
    }

    /**
     * Создает таблицы в базе данных, если они не существуют
     * @param connection соединение с базой данных
     */
    private static void createTables(DatabaseConnection connection) {
        // Таблица валют
        connection.executeUpdate(
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
        connection.executeUpdate(
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
        connection.executeUpdate(
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
        connection.executeUpdate(
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
        connection.executeUpdate(
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
     * Инициализирует дефолтные категории в базе данных
     * @param connection соединение с базой данных
     */
    private static void initializeDefaultCategories(DatabaseConnection connection) {
        // Check if categories already exist in table
        String countSql = "SELECT COUNT(*) FROM " + TABLE_CATEGORIES;
        int count = connection.executeQuerySingle(countSql, row -> row.getInt(1)).orElse(0);
        if (count > 0) {
            // Categories already exist, don't add
            return;
        }
        
        // Создаем родительские категории
        connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Доходы', 1, " + OPERATION_TYPE_INCOME + ", " + CATEGORY_TYPE_PARENT + ", NULL, 'initializer', datetime('now'))");
        connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Расходы', 2, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_PARENT + ", NULL, 'initializer', datetime('now'))");
        
        // Получаем ID созданных родительских категорий
        String parentCategoriesSql = "SELECT id, title, position FROM " + TABLE_CATEGORIES + " WHERE position IN (1, 2) ORDER BY position";
        List<Map<String, Object>> parentCategories = connection.executeQuery(parentCategoriesSql, row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row.getInt("id"));
            map.put("title", row.getString("title"));
            map.put("position", row.getInt("position"));
            return map;
        });
        
        int incomeParentId = 0, expenseParentId = 0;
        for (Map<String, Object> category : parentCategories) {
            int id = (Integer) category.get("id");
            int position = (Integer) category.get("position");
            
            if (position == 1) {
                incomeParentId = id;
            } else if (position == 2) {
                expenseParentId = id;
            }
        }
        
        // Проверяем, что ID получены корректно
        if (incomeParentId == 0 || expenseParentId == 0) {
            throw new RuntimeException("Не удалось получить ID родительских категорий");
        }
        
        // Создаем дочерние категории доходов
        String[] incomeCategoryTitles = {"Работа", "Подработка", "Подарки"};
        int incomePosition = 3;
        for (String title : incomeCategoryTitles) {
            connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + incomePosition + ", " + OPERATION_TYPE_INCOME + ", " + CATEGORY_TYPE_CHILD + ", " + incomeParentId + ", 'initializer', datetime('now'))");
            incomePosition++;
        }

        // Создаем промежуточные категории расходов
        connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Необходимые', 6, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + expenseParentId + ", 'initializer', datetime('now'))");
        connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('Дополнительные', 7, " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + expenseParentId + ", 'initializer', datetime('now'))");
        
        // Получаем ID созданных промежуточных категорий
        String intermediateCategoriesSql = "SELECT id, title, position FROM " + TABLE_CATEGORIES + " WHERE position IN (6, 7) ORDER BY position";
        List<Map<String, Object>> intermediateCategories = connection.executeQuery(intermediateCategoriesSql, row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row.getInt("id"));
            map.put("title", row.getString("title"));
            map.put("position", row.getInt("position"));
            return map;
        });
        
        int necessaryId = 0, additionalId = 0;
        for (Map<String, Object> category : intermediateCategories) {
            int id = (Integer) category.get("id");
            int position = (Integer) category.get("position");
            
            if (position == 6) {
                necessaryId = id;
            } else if (position == 7) {
                additionalId = id;
            }
        }
        
        // Проверяем, что ID получены корректно
        if (necessaryId == 0 || additionalId == 0) {
            throw new RuntimeException("Не удалось получить ID промежуточных категорий");
        }
        
        // Создаем дочерние категории необходимых расходов
        String[] necessaryCategoryTitles = {"Коммунальные", "Продукты", "Транспорт", "Медицина", "Одежда", "Налоги"};
        int necessaryPosition = 8;
        for (String title : necessaryCategoryTitles) {
            connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + necessaryPosition + ", " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + necessaryId + ", 'initializer', datetime('now'))");
            necessaryPosition++;
        }
        
        // Создаем дочерние категории дополнительных расходов
        String[] additionalCategoryTitles = {"Домашние нужды", "Кино", "Кафе и рестораны", "Подарки"};
        int additionalPosition = 14;
        for (String title : additionalCategoryTitles) {
            connection.executeUpdate("INSERT INTO " + TABLE_CATEGORIES + " (title, position, operation_type, type, parent_id, created_by, create_time) VALUES ('" + title + "', " + additionalPosition + ", " + OPERATION_TYPE_EXPENSE + ", " + CATEGORY_TYPE_CHILD + ", " + additionalId + ", 'initializer', datetime('now'))");
            additionalPosition++;
        }
    }
    
    /**
     * Инициализирует дефолтные валюты
     * @param connection соединение с базой данных
     */
    private static void initializeDefaultCurrencies(DatabaseConnection connection) {
        // Check if currencies already exist in table
        String countSql = "SELECT COUNT(*) FROM " + TABLE_CURRENCIES;
        int count = connection.executeQuerySingle(countSql, row -> row.getInt(1)).orElse(0);
        if (count > 0) {
            // Currencies already exist, don't add
            return;
        }
        
        // Массив валют: [название, позиция]
        String[][] currencies = {
            {"Рубль", "1"},
            {"Доллар", "2"},
            {"Евро", "3"},
        };
        
        for (String[] currencyInfo : currencies) {
            connection.executeUpdate("INSERT INTO " + TABLE_CURRENCIES + " (title, position, created_by, create_time) VALUES ('" + currencyInfo[0] + "', " + currencyInfo[1] + ", 'initializer', datetime('now'))");
        }
    }
    
    /**
     * Инициализирует дефолтные счета
     * @param connection соединение с базой данных
     */
    private static void initializeDefaultAccounts(DatabaseConnection connection) {
        System.out.println("🔄 initializeDefaultAccounts: Начинаем инициализацию счетов");
        
        // Check if accounts already exist in table
        String countSql = "SELECT COUNT(*) FROM " + TABLE_ACCOUNTS;
        int count = connection.executeQuerySingle(countSql, row -> row.getInt(1)).orElse(0);
        System.out.println("🔄 initializeDefaultAccounts: Текущее количество счетов: " + count);
        
        if (count > 0) {
            // Accounts already exist, don't add
            System.out.println("🔄 initializeDefaultAccounts: Счета уже существуют, пропускаем");
            return;
        }
        
        // Массив счетов: [название, тип, валюта, закрыт]
        String[][] accounts = {
            {"Наличные", String.valueOf(ACCOUNT_TYPE_CURRENT), String.valueOf(DEFAULT_CURRENCY_ID), String.valueOf(ACCOUNT_STATUS_OPEN)},
            {"Зарплатная карта", String.valueOf(ACCOUNT_TYPE_CURRENT), String.valueOf(DEFAULT_CURRENCY_ID), String.valueOf(ACCOUNT_STATUS_OPEN)},
            {"Сберегательный счет", String.valueOf(ACCOUNT_TYPE_SAVINGS), String.valueOf(DEFAULT_CURRENCY_ID), String.valueOf(ACCOUNT_STATUS_OPEN)},
            {"Кредитная карта", String.valueOf(ACCOUNT_TYPE_CREDIT), String.valueOf(DEFAULT_CURRENCY_ID), String.valueOf(ACCOUNT_STATUS_OPEN)},
            {"Карта рассрочки", String.valueOf(ACCOUNT_TYPE_CREDIT), String.valueOf(DEFAULT_CURRENCY_ID), String.valueOf(ACCOUNT_STATUS_OPEN)},
        };
        
        System.out.println("🔄 initializeDefaultAccounts: Добавляем " + accounts.length + " счетов");
        for (String[] acc : accounts) {
            String sql = "INSERT INTO " + TABLE_ACCOUNTS + " (title, amount, type, currency_id, closed, position, created_by, create_time) VALUES ('" + acc[0] + "', 0, " + acc[1] + ", " + acc[2] + ", " + acc[3] + ", " + acc[1] + ", 'initializer', datetime('now'))";
            System.out.println("🔄 initializeDefaultAccounts: Выполняем SQL: " + sql);
            connection.executeUpdate(sql);
        }
        
        // Проверяем результат
        int finalCount = connection.executeQuerySingle(countSql, row -> row.getInt(1)).orElse(0);
        System.out.println("🔄 initializeDefaultAccounts: Финальное количество счетов: " + finalCount);
    }
} 