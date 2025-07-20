package com.example.budgetmaster.backend.database;

import android.content.Context;

/**
 * Фабрика для создания Android Database Connection
 */
public class AndroidDatabaseFactory {
    
    private static Context context;
    
    public static void setContext(Context androidContext) {
        context = androidContext;
    }
    
    public static DatabaseConnection createConnection(String dbName) {
        if (context == null) {
            throw new IllegalStateException("Context not set. Call setContext() first.");
        }
        return new AndroidDatabaseConnection(context, dbName);
    }
} 