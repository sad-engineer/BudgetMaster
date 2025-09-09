package com.sadengineer.budgetmaster.backend.database;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.constants.DatabaseConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Менеджер для управления инициализацией базы данных
 * Использует явное управление транзакциями и асинхронные операции через Callable
 */
public class DatabaseManager {
    private static final String TAG = "DatabaseManager";

    private static final String DATABASE_NAME = DatabaseConstants.DATABASE_PATH;
    
    private final Context context;
    private final ExecutorService executor;
    
    public DatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = ThreadManager.getExecutor();
    }
    
    /**
     * Инициализирует базу данных при первом запуске приложения
     * Проверяет существование БД и создает дефолтные данные если нужно
     */
    public CompletableFuture<Boolean> initializeDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, DatabaseConstants.MSG_DATABASE_INIT_CHECK);
                
                // Получаем экземпляр базы данных
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
                
                // Проверяем, существует ли файл базы данных
                File databaseFile = context.getDatabasePath(DATABASE_NAME);
                boolean isNewDatabase = !databaseFile.exists();
                
                if (isNewDatabase) {
                    Log.d(TAG, DatabaseConstants.MSG_DATABASE_NOT_EXISTS);
                    
                    // Проверяем, есть ли дефолтные данные
                    int currencyCount = database.currencyDao().count(EntityFilter.ALL);
                    int categoryCount = database.categoryDao().count(EntityFilter.ALL);
                    int accountCount = database.accountDao().count(EntityFilter.ALL);
                    
                    if (currencyCount == 0 && categoryCount == 0 && accountCount == 0) {
                        Log.d(TAG, DatabaseConstants.MSG_CREATE_DEFAULT_DATA);
                        DatabaseInitializer.initializeDefaultData(database);
                        Log.d(TAG, DatabaseConstants.MSG_DEFAULT_DATA_CREATED);
                    } else {
                        Log.d(TAG, DatabaseConstants.MSG_DEFAULT_DATA_EXISTS);
                    }
                } else {
                    Log.d(TAG, DatabaseConstants.MSG_DATABASE_EXISTS);
                    
                    // Проверяем, есть ли данные
                    int currencyCount = database.currencyDao().count(EntityFilter.ALL);
                    int categoryCount = database.categoryDao().count(EntityFilter.ALL);
                    int accountCount = database.accountDao().count(EntityFilter.ALL);
                    
                    Log.d(TAG, String.format(DatabaseConstants.MSG_DATA_STATISTICS, 
                          currencyCount, categoryCount, accountCount));
                }
                
                Log.d(TAG, DatabaseConstants.MSG_DATABASE_INIT_COMPLETE);
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, DatabaseConstants.MSG_DATABASE_INIT_ERROR + e.getMessage(), e);
                return false;
            }
        }, executor);
    }
}