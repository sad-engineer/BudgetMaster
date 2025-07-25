// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.database;

import android.content.Context;

/**
 * Android утилита для инициализации провайдера БД
 */
public class AndroidPlatformUtil {
    
    /**
     * Инициализирует Android провайдер БД
     * @param context Android контекст
     */
    public static void initializeDatabaseProvider(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Android Context не может быть null");
        }
        
        // Инициализируем Android провайдер БД
        AndroidDatabaseFactory.initialize(context);
        System.out.println("Инициализирован Android провайдер БД");
    }
    
    /**
     * Проверяет, что это Android платформа
     * @return true (всегда true для Android)
     */
    public static boolean isAndroid() {
        return true;
    }
} 