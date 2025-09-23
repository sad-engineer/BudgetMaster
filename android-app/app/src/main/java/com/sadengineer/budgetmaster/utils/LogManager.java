package com.sadengineer.budgetmaster.utils;

import android.content.Context;
import android.util.Log;

/**
 * Менеджер для управления логированием в приложении
 */
public class LogManager {
    
    private static LogConfigManager configManager = LogConfigManager.getInstance();
    private static boolean isInitialized = false;
    
    /**
     * Инициализирует LogManager с контекстом
     */
    public static void initialize(Context context) {
        if (!isInitialized) {
            configManager.loadConfig(context);
            isInitialized = true;
        }
    }

    /**
     * Проверяет, нужно ли логировать с данным уровнем
     */
    public static boolean shouldLogWithLevel(String tag, String level) {
        return configManager.shouldLogWithLevel(tag, level);
    }
    
    /**
     * Логирует Debug сообщение с проверкой уровня
     */
    public static void d(String tag, String message) {
        if (shouldLogWithLevel(tag, "DEBUG")) {
            Log.d(tag, message);
        }
    }
    
    /**
     * Логирует Debug сообщение с исключением
     */
    public static void d(String tag, String message, Throwable throwable) {
        if (shouldLogWithLevel(tag, "DEBUG")) {
            Log.d(tag, message, throwable);
        }
    }
    
    /**
     * Логирует Error сообщение с проверкой уровня
     */
    public static void e(String tag, String message) {
        if (shouldLogWithLevel(tag, "ERROR")) {
            Log.e(tag, message);
        }
    }
    
    /**
     * Логирует Error сообщение с исключением
     */
    public static void e(String tag, String message, Throwable throwable) {
        if (shouldLogWithLevel(tag, "ERROR")) {
            Log.e(tag, message, throwable);
        }
    }
    
    /**
     * Логирует Warning сообщение с проверкой уровня
     */
    public static void w(String tag, String message) {
        if (shouldLogWithLevel(tag, "WARN")) {
            Log.w(tag, message);
        }
    }
    
    /**
     * Логирует Info сообщение с проверкой уровня
     */
    public static void i(String tag, String message) {
        if (shouldLogWithLevel(tag, "INFO")) {
            Log.i(tag, message);
        }
    }
    
    /**
     * Логирует Verbose сообщение с проверкой уровня
     */
    public static void v(String tag, String message) {
        if (shouldLogWithLevel(tag, "VERBOSE")) {
            Log.v(tag, message);
        }
    }
}
