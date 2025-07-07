// -*- coding: utf-8 -*-
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
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
    
    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Currencies table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS currencies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    position INTEGER NOT NULL,
                    title TEXT NOT NULL UNIQUE
                )
            """);
            // Accounts table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    position INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    currency_id INTEGER NOT NULL,
                    closed INTEGER NOT NULL DEFAULT 0,
                    credit_card_limit INTEGER,
                    credit_card_category_id INTEGER,
                    credit_card_commission_category_id INTEGER,
                    FOREIGN KEY (currency_id) REFERENCES currencies (id)
                )
            """);
            // Categories table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    position INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    operation_type INTEGER NOT NULL,
                    type INTEGER NOT NULL,
                    parent_id INTEGER,
                    FOREIGN KEY (parent_id) REFERENCES categories (id)
                )
            """);
            // Budgets table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
                    amount INTEGER NOT NULL,
                    currency_id INTEGER NOT NULL,
                    category_id INTEGER,
                    FOREIGN KEY (currency_id) REFERENCES currencies (id),
                    FOREIGN KEY (category_id) REFERENCES categories (id)
                )
            """);
            // Operations table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS operations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    create_time TIMESTAMP NOT NULL,
                    update_time TIMESTAMP,
                    delete_time TIMESTAMP,
                    created_by TEXT,
                    updated_by TEXT,
                    deleted_by TEXT,
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
                    FOREIGN KEY (category_id) REFERENCES categories (id),
                    FOREIGN KEY (account_id) REFERENCES accounts (id),
                    FOREIGN KEY (currency_id) REFERENCES currencies (id),
                    FOREIGN KEY (to_account_id) REFERENCES accounts (id),
                    FOREIGN KEY (to_currency_id) REFERENCES currencies (id)
                )
            """);
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
        
        // Add default currencies
        String[] currencies = {
            "INSERT INTO currencies (create_time, update_time, created_by, updated_by, position, title) VALUES (datetime('now'), datetime('now'), 'system', 'system', 1, 'RUB')",
            "INSERT INTO currencies (create_time, update_time, created_by, updated_by, position, title) VALUES (datetime('now'), datetime('now'), 'system', 'system', 1, 'USD')",
            "INSERT INTO currencies (create_time, update_time, created_by, updated_by, position, title) VALUES (datetime('now'), datetime('now'), 'system', 'system', 1, 'EUR')"
        };
        
        try (Statement stmt = conn.createStatement()) {
            for (String sql : currencies) {
                stmt.executeUpdate(sql);
            }
        }
    }
} 