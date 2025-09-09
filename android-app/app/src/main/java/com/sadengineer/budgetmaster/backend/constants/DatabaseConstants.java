package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для DatabaseManager
 */
public class DatabaseConstants {

    // ========================================
    // НАСТРОЙКИ БАЗЫ ДАННЫХ
    // ========================================
    
    // Путь к базе данных
    public static final String DATABASE_PATH = "budget_master.db";
    
    // ========================================
    // СООБЩЕНИЯ ДЛЯ DATABASE MANAGER
    // ========================================
    
    public static final String MSG_DATABASE_INIT_CHECK = "Проверка инициализации базы данных...";
    public static final String MSG_DATABASE_NOT_EXISTS = "База данных не существует, создаем новую...";
    public static final String MSG_DATABASE_EXISTS = "База данных уже существует";
    public static final String MSG_DATABASE_INIT_COMPLETE = "Инициализация базы данных завершена";
    public static final String MSG_CREATE_DEFAULT_DATA = "Создаем дефолтные данные...";
    public static final String MSG_DEFAULT_DATA_CREATED = "Дефолтные данные созданы успешно";
    public static final String MSG_DEFAULT_DATA_EXISTS = "Дефолтные данные уже существуют";
    public static final String MSG_DATA_STATISTICS = "Статистика данных: %d валют, %d категорий, %d счетов";
    public static final String MSG_DATABASE_INIT_ERROR = "Ошибка инициализации базы данных: ";

}