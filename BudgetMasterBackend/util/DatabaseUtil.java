// -*- coding: utf-8 -*-
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Currency;
import model.Category;
import repository.CurrencyRepository;
import repository.CategoryRepository;

public class DatabaseUtil {
    /**
     * Clears all data from all tables
     * @param dbPath path to database file
     * @throws SQLException if database operation fails
     */
    public static void clearAllData(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                // Disable foreign key constraints temporarily
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // Clear all tables in reverse dependency order
                stmt.execute("DELETE FROM operations");
                stmt.execute("DELETE FROM budgets");
                stmt.execute("DELETE FROM accounts");
                stmt.execute("DELETE FROM categories");
                stmt.execute("DELETE FROM currencies");
                
                // Reset auto-increment counters
                stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('operations', 'budgets', 'accounts', 'categories', 'currencies')");
                
                // Re-enable foreign key constraints
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
    }
    
    /**
     * Clears data from specific table
     * @param dbPath path to database file
     * @param tableName name of table to clear
     * @throws SQLException if database operation fails
     */
    public static void clearTable(String dbPath, String tableName) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement stmt = conn.createStatement()) {
                // Disable foreign key constraints temporarily
                stmt.execute("PRAGMA foreign_keys = OFF");
                
                // Clear table
                stmt.execute("DELETE FROM " + tableName);
                
                // Reset auto-increment counter
                stmt.execute("DELETE FROM sqlite_sequence WHERE name = '" + tableName + "'");
                
                // Re-enable foreign key constraints
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
    }
    
    public static void createDatabaseIfNotExists(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Set UTF-8 encoding for database
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA encoding = 'UTF-8'");
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            // Create tables
            createTables(conn);
            
            // Initialize default currencies
            initializeDefaultCurrencies(conn);
        }
    }
    
    /**
     * Gets count of records in specified table
     * @param dbPath path to database file
     * @param tableName name of table
     * @return number of records
     * @throws SQLException if database operation fails
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
     * Gets count of all records in all tables
     * @param dbPath path to database file
     * @return total number of records
     * @throws SQLException if database operation fails
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
     * Restores default categories only
     * @param dbPath path to database file
     * @throws SQLException if database operation fails
     */
    public static void restoreDefaultCategories(String dbPath) throws SQLException {
        // Clear categories table
        clearTable(dbPath, "categories");
        
        // Reinitialize categories
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCategories(conn);
        }
    }
    
    /**
     * Restores default currencies only
     * @param dbPath path to database file
     * @throws SQLException if database operation fails
     */
    public static void restoreDefaultCurrencies(String dbPath) throws SQLException {
        // Clear currencies table
        clearTable(dbPath, "currencies");
        
        // Reinitialize currencies
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCurrencies(conn);
        }
    }
    
    /**
     * Restores default values to the database
     * @param dbPath path to database file
     * @throws SQLException if database operation fails
     */
    public static void restoreDefaults(String dbPath) throws SQLException {
        // Clear all data first
        clearAllData(dbPath);
        
        // Reinitialize with defaults
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url)) {
            initializeDefaultCurrencies(conn);
            initializeDefaultCategories(conn);
        }
    }
    
    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Currencies table
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
            // Accounts table
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
            // Categories table
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
            // Budgets table
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
            // Operations table
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
        
        // Add default categories
        String[][] categories = {
            // title, operation_type, type
            {"Продукты", "1", "1"},      // Расходы, Основные
            {"Транспорт", "1", "1"},     // Расходы, Основные
            {"Развлечения", "1", "1"},   // Расходы, Основные
            {"Зарплата", "2", "1"},      // Доходы, Основные
            {"Подработка", "2", "1"},    // Доходы, Основные
            {"Подарки", "1", "2"},       // Расходы, Дополнительные
            {"Бонусы", "2", "2"}         // Доходы, Дополнительные
        };
        
        for (String[] categoryData : categories) {
            Category category = new Category();
            category.setPosition(0); // Will be auto-assigned by repository
            category.setTitle(categoryData[0]);
            category.setOperationType(Integer.parseInt(categoryData[1]));
            category.setType(Integer.parseInt(categoryData[2]));
            category.setParentId(null);
            category.setCreatedBy("initializer");
            category.setUpdatedBy("initializer");
            category.setCreateTime(java.time.LocalDateTime.now());
            category.setUpdateTime(java.time.LocalDateTime.now());
            category.setDeleteTime(null);
            
            categoryRepo.save(category);
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
        String[] currencyTitles = {"RUB", "USD", "EUR"};
        for (String title : currencyTitles) {
            Currency currency = new Currency();
            currency.setPosition(0); // Will be auto-assigned by repository
            currency.setTitle(title);
            currency.setCreatedBy("initializer");
            currency.setUpdatedBy("initializer");
            currency.setCreateTime(java.time.LocalDateTime.now());
            currency.setUpdateTime(java.time.LocalDateTime.now());
            currency.setDeleteTime(null);
            
            currencyRepo.save(currency);
        }    
    }
} 