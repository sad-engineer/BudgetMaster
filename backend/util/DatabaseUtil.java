// -*- coding: utf-8 -*-
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Currency;
import model.Category;
import model.Account;
import repository.CurrencyRepository;
import repository.CategoryRepository;
import repository.AccountRepository;
import static constants.RepositoryConstants.*;
import static constants.ModelConstants.ACCOUNT_TYPE_CURRENT;
import static constants.ModelConstants.DEFAULT_CURRENCY_ID;
import static constants.ModelConstants.ACCOUNT_STATUS_OPEN;
import static constants.ModelConstants.ACCOUNT_TYPE_SAVINGS;
import static constants.ModelConstants.ACCOUNT_TYPE_CREDIT;
import static constants.ModelConstants.OPERATION_TYPE_INCOME;
import static constants.ModelConstants.OPERATION_TYPE_EXPENSE;
import static constants.ModelConstants.CATEGORY_TYPE_PARENT;
import static constants.ModelConstants.CATEGORY_TYPE_CHILD;
import static constants.ModelConstants.ACCOUNT_STATUS_OPEN;


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
    public static void createDatabaseIfNotExists(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Устанавливаем кодировку UTF-8 для базы
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA encoding = 'UTF-8'");
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            // Создаем таблицы
            createTables(conn);
            // Инициализируем дефолтные валюты
            initializeDefaultCurrencies(conn);
            // Инициализируем дефолтные категории
            initializeDefaultCategories(conn);
            // Инициализируем дефолтные счета
            initializeDefaultAccounts(conn);
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
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCategories(conn);
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
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCurrencies(conn);
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
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCurrencies(conn);
            initializeDefaultCategories(conn);
            initializeDefaultAccounts(conn);
        }
    }
    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Таблица валют
            stmt.executeUpdate(
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
            stmt.executeUpdate(
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
            stmt.executeUpdate(
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
            stmt.executeUpdate(
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
            stmt.executeUpdate(
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
    }
    
    private static void initializeDefaultCategories(Connection conn) throws SQLException {
        // Check if categories already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE_CATEGORIES)) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Categories already exist, don't add
                return;
            }
        }
        
        // Use repository to add default categories with auto-position
        String dbPath = conn.getMetaData().getURL().replace("jdbc:sqlite:", "");
        CategoryRepository categoryRepo = new CategoryRepository(dbPath);
                
        // Сначала создаем родительские категории
        Category incomeParent = new Category();
        incomeParent.setTitle("Доходы");
        incomeParent.setPosition(1);
        incomeParent.setOperationType(OPERATION_TYPE_INCOME); // Доходы
        incomeParent.setType(CATEGORY_TYPE_PARENT); // Родительская
        incomeParent.setParentId(null);
        incomeParent.setCreatedBy("initializer");
        incomeParent.setUpdatedBy(null);
        incomeParent.setCreateTime(java.time.LocalDateTime.now());
        incomeParent.setUpdateTime(null);
        incomeParent.setDeleteTime(null);
        
        Category expenseParent = new Category();
        expenseParent.setTitle("Расходы");
        expenseParent.setPosition(2);
        expenseParent.setOperationType(OPERATION_TYPE_EXPENSE); // Расходы
        expenseParent.setType(CATEGORY_TYPE_PARENT); // Родительская
        expenseParent.setParentId(null);
        expenseParent.setCreatedBy("initializer");
        expenseParent.setUpdatedBy(null);
        expenseParent.setCreateTime(java.time.LocalDateTime.now());
        expenseParent.setUpdateTime(null);
        expenseParent.setDeleteTime(null);
        
        // Сохраняем родительские категории
        Category savedIncomeParent = categoryRepo.save(incomeParent);
        Category savedExpenseParent = categoryRepo.save(expenseParent);
        
        // Создаем дочерние категории доходов
        String[] incomeCategoryTitles = {"Работа", "Подработка", "Подарки"};
        int incomePosition = 3;
        for (String title : incomeCategoryTitles) {
            Category category = new Category();
            category.setTitle(title);
            category.setPosition(incomePosition);
            category.setOperationType(OPERATION_TYPE_INCOME); // Доходы
            category.setType(CATEGORY_TYPE_CHILD); // Дочерняя
            category.setParentId(savedIncomeParent.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy(null);
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(null);
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
            incomePosition++;
        }

        // Создаем промежуточные категории расходов
        Category necessaryExpense = new Category();
        necessaryExpense.setTitle("Необходимые");
        necessaryExpense.setPosition(6);
        necessaryExpense.setOperationType(OPERATION_TYPE_EXPENSE); // Расходы
        necessaryExpense.setType(CATEGORY_TYPE_CHILD); // Дочерняя
        necessaryExpense.setParentId(savedExpenseParent.getId());
        necessaryExpense.setCreatedBy("initializer");
        necessaryExpense.setUpdatedBy(null);
        necessaryExpense.setCreateTime(java.time.LocalDateTime.now());
        necessaryExpense.setUpdateTime(null);
        necessaryExpense.setDeleteTime(null);
        
        Category additionalExpense = new Category();
        additionalExpense.setTitle("Дополнительные");
        additionalExpense.setPosition(7);
        additionalExpense.setOperationType(OPERATION_TYPE_EXPENSE); // Расходы
        additionalExpense.setType(CATEGORY_TYPE_CHILD); // Дочерняя
        additionalExpense.setParentId(savedExpenseParent.getId());
        additionalExpense.setCreatedBy("initializer");
        additionalExpense.setUpdatedBy(null);
        additionalExpense.setCreateTime(java.time.LocalDateTime.now());
        additionalExpense.setUpdateTime(null);
        additionalExpense.setDeleteTime(null);
        
        // Сохраняем промежуточные категории
        Category savedNecessary = categoryRepo.save(necessaryExpense);
        Category savedAdditional = categoryRepo.save(additionalExpense);
        
        // Создаем дочерние категории необходимых расходов
        String[] necessaryCategoryTitles = {"Коммунальные", "Продукты", "Транспорт", "Медицина", "Одежда", "Налоги"};
        int necessaryPosition = 8;
        for (String title : necessaryCategoryTitles) {
            Category category = new Category();
            category.setTitle(title);
            category.setPosition(necessaryPosition);
            category.setOperationType(OPERATION_TYPE_EXPENSE); // Расходы
            category.setType(CATEGORY_TYPE_CHILD); // Дочерняя
            category.setParentId(savedNecessary.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy(null);
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(null);
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
            necessaryPosition++;
        }
        
        // Создаем дочерние категории дополнительных расходов
        String[] additionalCategoryTitles = {"Домашние нужды", "Кино", "Кафе и рестораны", "Подарки"};
        int additionalPosition = 14;
        for (String title : additionalCategoryTitles) {
            Category category = new Category();
            category.setTitle(title);
            category.setPosition(additionalPosition);
            category.setOperationType(OPERATION_TYPE_EXPENSE); // Расходы
            category.setType(CATEGORY_TYPE_CHILD); // Дочерняя
            category.setParentId(savedAdditional.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy(null);
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(null);
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
            additionalPosition++;
        }
    }
    
    private static void initializeDefaultCurrencies(Connection conn) throws SQLException {
        // Check if currencies already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE_CURRENCIES)) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Currencies already exist, don't add
                return;
            }
        }
        
        // Use repository to add default currencies with auto-position
        String dbPath = conn.getMetaData().getURL().replace("jdbc:sqlite:", "");
        CurrencyRepository currencyRepo = new CurrencyRepository(dbPath);
        
        // Add default currencies
        Object[][] currencyData = {
            {"RUB", 1},
            {"USD", 2},
            {"EUR", 3}
        };
        for (Object[] currencyInfo : currencyData) {
            Currency currency = new Currency();
            currency.setTitle((String) currencyInfo[0]);
            currency.setPosition((int) currencyInfo[1]);
            currency.setCreatedBy("initializer");
            currency.setUpdatedBy(null);
            currency.setCreateTime(java.time.LocalDateTime.now());
            currency.setUpdateTime(null);
            currency.setDeleteTime(null);
            
            currencyRepo.save(currency);
        }    
    }

    private static void initializeDefaultAccounts(Connection conn) throws SQLException {
        // Check if accounts already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE_ACCOUNTS)) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Accounts already exist, don't add
                return;
            }
        }
        String dbPath = conn.getMetaData().getURL().replace("jdbc:sqlite:", "");
        AccountRepository accountRepo = new AccountRepository(dbPath);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // title, type, currencyId, closed, position
        Object[][] defaultAccounts = {
            {"Наличные", ACCOUNT_TYPE_CURRENT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 1},
            {"Зарплатная карта", ACCOUNT_TYPE_CURRENT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 2},
            {"Сберегательный счет", ACCOUNT_TYPE_SAVINGS, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 3},
            {"Кредитная карта", ACCOUNT_TYPE_CREDIT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 4},
            {"Карта рассрочки", ACCOUNT_TYPE_CREDIT, DEFAULT_CURRENCY_ID, ACCOUNT_STATUS_OPEN, 5},
        };
        for (Object[] acc : defaultAccounts) {
            Account account = new Account();
            account.setTitle((String) acc[0]);
            account.setAmount(0);
            account.setType((int) acc[1]);
            account.setCurrencyId((int) acc[2]);
            account.setClosed((int) acc[3]);
            account.setPosition((int) acc[4]);
            account.setCreditCardLimit(null);
            account.setCreditCardCategoryId(null);
            account.setCreditCardCommissionCategoryId(null);
            account.setCreatedBy("initializer");
            account.setUpdatedBy(null);
            account.setDeletedBy(null);
            account.setCreateTime(now);
            account.setUpdateTime(null);
            account.setDeleteTime(null);
            accountRepo.save(account);
        }
    }
} 