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
                stmt.execute("DELETE FROM operations");
                stmt.execute("DELETE FROM budgets");
                stmt.execute("DELETE FROM accounts");
                stmt.execute("DELETE FROM categories");
                stmt.execute("DELETE FROM currencies");
                // Сбрасываем счетчики автоинкремента
                stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('operations', 'budgets', 'accounts', 'categories', 'currencies')");
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
        String[] tables = {"currencies", "categories", "accounts", "budgets", "operations"};
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
        clearTable(dbPath, "categories");
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
        clearTable(dbPath, "currencies");
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
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS currencies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP
                )
            """);
            // Таблица счетов
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    amount INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    currency_id INTEGER NOT NULL,
                    closed INTEGER NOT NULL DEFAULT 0,
                    credit_card_limit INTEGER,
                    credit_card_category_id INTEGER,
                    credit_card_commission_category_id INTEGER,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    FOREIGN KEY (currency_id) REFERENCES currencies (id)
                )
            """);
            // Таблица категорий
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    operation_type INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    parent_id INTEGER,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    FOREIGN KEY (parent_id) REFERENCES categories (id)
                )
            """);
            // Таблица бюджетов
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    amount INTEGER NOT NULL,
                    currency_id INTEGER NOT NULL,
                    category_id INTEGER,
                    position INTEGER NOT NULL,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    FOREIGN KEY (currency_id) REFERENCES currencies (id),
                    FOREIGN KEY (category_id) REFERENCES categories (id)
                )
            """);
            // Таблица операций
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS operations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type INTEGER NOT NULL,
                    date TIMESTAMP NOT NULL,
                    amount INTEGER NOT NULL,
                    comment TEXT NOT NULL,
                    category_id INTEGER NOT NULL,
                    account_id INTEGER NOT NULL,
                    currency_id INTEGER NOT NULL,
                    to_account_id INTEGER,
                    to_currency_id INTEGER,
                    to_amount INTEGER,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    FOREIGN KEY (category_id) REFERENCES categories (id),
                    FOREIGN KEY (account_id) REFERENCES accounts (id),
                    FOREIGN KEY (currency_id) REFERENCES currencies (id),
                    FOREIGN KEY (to_account_id) REFERENCES accounts (id),
                    FOREIGN KEY (to_currency_id) REFERENCES currencies (id)
                )
            """);
        }
    }
    
    private static void initializeDefaultCategories(Connection conn) throws SQLException {
        // Check if categories already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
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
        incomeParent.setOperationType(2); // Доходы
        incomeParent.setType(0); // Родительская
        incomeParent.setParentId(null);
        incomeParent.setCreatedBy("initializer");
        incomeParent.setUpdatedBy("initializer");
        incomeParent.setCreateTime(java.time.LocalDateTime.now());
        incomeParent.setUpdateTime(java.time.LocalDateTime.now());
        incomeParent.setDeleteTime(null);
        
        Category expenseParent = new Category();
        expenseParent.setTitle("Расходы");
        expenseParent.setPosition(2);
        expenseParent.setOperationType(1); // Расходы
        expenseParent.setType(0); // Родительская
        expenseParent.setParentId(null);
        expenseParent.setCreatedBy("initializer");
        expenseParent.setUpdatedBy("initializer");
        expenseParent.setCreateTime(java.time.LocalDateTime.now());
        expenseParent.setUpdateTime(java.time.LocalDateTime.now());
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
            category.setOperationType(2); // Доходы
            category.setType(1); // Дочерняя
            category.setParentId(savedIncomeParent.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy("initializer");
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(java.time.LocalDateTime.now());
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
            incomePosition++;
        }

        // Создаем промежуточные категории расходов
        Category necessaryExpense = new Category();
        necessaryExpense.setTitle("Необходимые");
        necessaryExpense.setPosition(6);
        necessaryExpense.setOperationType(1); // Расходы
        necessaryExpense.setType(1); // Дочерняя
        necessaryExpense.setParentId(savedExpenseParent.getId());
        necessaryExpense.setCreatedBy("initializer");
        necessaryExpense.setUpdatedBy("initializer");
        necessaryExpense.setCreateTime(java.time.LocalDateTime.now());
        necessaryExpense.setUpdateTime(java.time.LocalDateTime.now());
        necessaryExpense.setDeleteTime(null);
        
        Category additionalExpense = new Category();
        additionalExpense.setTitle("Дополнительные");
        additionalExpense.setPosition(7);
        additionalExpense.setOperationType(1); // Расходы
        additionalExpense.setType(1); // Дочерняя
        additionalExpense.setParentId(savedExpenseParent.getId());
        additionalExpense.setCreatedBy("initializer");
        additionalExpense.setUpdatedBy("initializer");
        additionalExpense.setCreateTime(java.time.LocalDateTime.now());
        additionalExpense.setUpdateTime(java.time.LocalDateTime.now());
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
            category.setOperationType(1); // Расходы
            category.setType(1); // Дочерняя
            category.setParentId(savedNecessary.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy("initializer");
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(java.time.LocalDateTime.now());
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
            category.setOperationType(1); // Расходы
            category.setType(1); // Дочерняя
            category.setParentId(savedAdditional.getId());
            category.setCreatedBy("initializer");
            category.setUpdatedBy("initializer");
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(java.time.LocalDateTime.now());
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
            additionalPosition++;
        }
    }
    
    private static void initializeDefaultCurrencies(Connection conn) throws SQLException {
        // Check if currencies already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM currencies")) {
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
            currency.setUpdatedBy("initializer");
            currency.setCreateTime(java.time.LocalDateTime.now());
            currency.setUpdateTime(java.time.LocalDateTime.now());
            currency.setDeleteTime(null);
            
            currencyRepo.save(currency);
        }    
    }

    private static void initializeDefaultAccounts(Connection conn) throws SQLException {
        // Check if accounts already exist in table
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts")) {
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
            {"Наличные", 1, 1, 0, 1},
            {"Зарплатная карта", 1, 1, 0, 2},
            {"Сберегательный счет", 2, 1, 0, 3},
            {"Кредитная карта", 3, 1, 0, 4},
            {"Карта рассрочки", 3, 1, 0, 5},
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
            account.setUpdatedBy("initializer");
            account.setDeletedBy(null);
            account.setCreateTime(now);
            account.setUpdateTime(now);
            account.setDeleteTime(null);
            accountRepo.save(account);
        }
    }
} 