// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.util;

import com.sadengineer.budgetmaster.backend.database.DatabaseFactory;
import com.sadengineer.budgetmaster.backend.database.jdbc.JdbcDatabaseFactory;

/**
 * Утилита для определения платформы и инициализации соответствующего провайдера БД
 */
public class PlatformUtil {
    
    private static boolean isAndroid = false;
    private static boolean isInitialized = false;
    
    /**
     * Определяет, запущено ли приложение на Android
     * @return true если это Android платформа
     */
    public static boolean isAndroid() {
        if (!isInitialized) {
            try {
                // Пытаемся загрузить Android классы
                Class.forName("android.content.Context");
                isAndroid = true;
            } catch (ClassNotFoundException e) {
                isAndroid = false;
            }
            isInitialized = true;
        }
        return isAndroid;
    }
    
    /**
     * Инициализирует соответствующий провайдер БД в зависимости от платформы
     * @param androidContext контекст Android (может быть null для JDBC)
     */
    public static void initializeDatabaseProvider(Object androidContext) {
        if (isAndroid()) {
            if (androidContext == null) {
                throw new IllegalArgumentException("Android Context не может быть null на Android платформе");
            }
            // На Android платформе инициализация будет происходить в Android приложении
            System.out.println("Android провайдер БД должен быть инициализирован в Android приложении");
        } else {
            JdbcDatabaseFactory.initialize();
            System.out.println("Инициализирован JDBC провайдер БД");
        }
    }
    
    /**
     * Инициализирует JDBC провайдер (для тестирования или принудительного использования)
     */
    public static void initializeJdbcProvider() {
        JdbcDatabaseFactory.initialize();
        System.out.println("Принудительно инициализирован JDBC провайдер БД");
    }
    
    /**
     * Инициализирует Android провайдер (для тестирования или принудительного использования)
     * @param androidContext контекст Android
     */
    public static void initializeAndroidProvider(Object androidContext) {
        if (androidContext == null) {
            throw new IllegalArgumentException("Android Context не может быть null");
        }
        // Android провайдер будет инициализирован в Android приложении
        System.out.println("Android провайдер БД должен быть инициализирован в Android приложении");
    }
} 