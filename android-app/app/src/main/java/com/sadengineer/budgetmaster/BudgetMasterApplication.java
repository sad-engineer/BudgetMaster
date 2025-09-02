package com.sadengineer.budgetmaster;

import android.app.Application;
import android.util.Log;

import com.sadengineer.budgetmaster.backend.ThreadManager;

/**
 * Главный класс приложения
 * Управляет глобальными ресурсами и их жизненным циклом
 */
public class BudgetMasterApplication extends Application {
    
    private static final String TAG = "BudgetMasterApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Приложение инициализировано");
        
        // Здесь можно инициализировать глобальные компоненты
        // например, Crashlytics, Analytics, и т.д.
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Приложение завершается");
        
        // Завершаем ThreadManager при завершении приложения
        ThreadManager.shutdown();
    }
    
    /**
     * Принудительно завершить ThreadManager
     * Вызывать только в крайних случаях
     */
    public void forceShutdownThreadManager() {
        Log.w(TAG, "Принудительное завершение ThreadManager");
        ThreadManager.shutdownNow();
    }
    
    /**
     * Проверить состояние ThreadManager
     * @return true, если ThreadManager завершен
     */
    public boolean isThreadManagerShutdown() {
        return ThreadManager.isShutdown();
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "Приложению не хватает памяти");
        
        // Можно освободить кэши и временные ресурсы
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "Система запросила освобождение памяти, уровень: " + level);
        
        // Освобождаем ресурсы в зависимости от уровня
        if (level >= TRIM_MEMORY_MODERATE) {
            // Освобождаем кэши и временные ресурсы
        }
    }
}
