// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.database;

import android.content.Context;

import com.sadengineer.budgetmaster.backend.database.DatabaseConnection;
import com.sadengineer.budgetmaster.backend.database.DatabaseFactory;

/**
 * Android фабрика для создания соединений с базой данных
 */
public class AndroidDatabaseFactory implements DatabaseFactory.DatabaseProvider {
    
    private final Context androidContext;
    
    /**
     * Конструктор Android фабрики
     * @param context Android контекст
     */
    public AndroidDatabaseFactory(Context context) {
        this.androidContext = context;
    }
    
    /**
     * Создает Android соединение с базой данных
     * @param dbPath путь к файлу БД
     * @return Android соединение
     */
    @Override
    public DatabaseConnection createConnection(String dbPath) {
        return new AndroidDatabaseConnection(androidContext, dbPath);
    }
    
    /**
     * Инициализирует Android как провайдер по умолчанию
     * @param context Android контекст
     */
    public static void initialize(Context context) {
        DatabaseFactory.setProvider(new AndroidDatabaseFactory(context));
    }
} 