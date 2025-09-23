package com.sadengineer.budgetmaster;

import android.app.Application;

import com.sadengineer.budgetmaster.utils.LogManager;
/**
 * Главный класс приложения
 */
public class BudgetMasterApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Инициализируем LogManager с конфигурацией из JSON
        LogManager.initialize(this);
    }
}