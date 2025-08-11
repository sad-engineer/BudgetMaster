package com.sadengineer.budgetmaster.backend.database;

import android.content.Context;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.dao.CurrencyDao;
import com.sadengineer.budgetmaster.backend.dao.CategoryDao;
import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Account;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Менеджер для управления инициализацией базы данных
 * Использует явное управление транзакциями и асинхронные операции через Callable
 */
public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String DATABASE_NAME = "budget_master_database";
    
    private final Context context;
    private final ExecutorService executor;
    
    public DatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Инициализирует базу данных при первом запуске приложения
     * Проверяет существование БД и создает дефолтные данные если нужно
     */
    public CompletableFuture<Boolean> initializeDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "🔍 Проверка инициализации базы данных...");
                
                // Получаем экземпляр базы данных
                BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
                
                // Проверяем, существует ли файл базы данных
                File databaseFile = context.getDatabasePath(DATABASE_NAME);
                boolean isNewDatabase = !databaseFile.exists();
                
                if (isNewDatabase) {
                    Log.d(TAG, "📁 База данных не существует, создаем новую...");
                    
                    // Проверяем, есть ли дефолтные данные
                    int currencyCount = database.currencyDao().count();
                    int categoryCount = database.categoryDao().count();
                    int accountCount = database.accountDao().count();
                    
                    if (currencyCount == 0 && categoryCount == 0 && accountCount == 0) {
                        Log.d(TAG, "📝 Создаем дефолтные данные...");
                        DatabaseInitializer.initializeDefaultData(database);
                        Log.d(TAG, "✅ Дефолтные данные созданы успешно");
                    } else {
                        Log.d(TAG, "ℹ️ Дефолтные данные уже существуют");
                    }
                } else {
                    Log.d(TAG, "📁 База данных уже существует");
                    
                    // Проверяем, есть ли данные
                    int currencyCount = database.currencyDao().count();
                    int categoryCount = database.categoryDao().count();
                    int accountCount = database.accountDao().count();
                    
                    Log.d(TAG, "📊 Статистика данных: " + 
                          currencyCount + " валют, " + 
                          categoryCount + " категорий, " + 
                          accountCount + " счетов");
                }
                
                Log.d(TAG, "✅ Инициализация базы данных завершена");
                return true;
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка инициализации базы данных: " + e.getMessage(), e);
                return false;
            }
        }, executor);
    }
    
    /**
     * Выполняет операцию с базой данных в фоновом потоке
     */
    public <T> CompletableFuture<T> executeDatabaseOperation(Callable<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка выполнения операции с БД: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Закрывает executor
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
} 