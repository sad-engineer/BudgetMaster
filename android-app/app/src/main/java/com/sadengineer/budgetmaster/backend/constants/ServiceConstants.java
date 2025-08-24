package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для сервисов
 */
public class ServiceConstants {

    // База данных (ссылаемся на RepositoryConstants)
    public static final String DEFAULT_DATABASE_NAME = RepositoryConstants.DATABASE_PATH;

    // Значения по умолчанию для счетов (ссылаемся на ModelConstants)
    public static final long DEFAULT_ACCOUNT_BALANCE = ModelConstants.DEFAULT_ACCOUNT_BALANCE;  // Начальный баланс по умолчанию
    public static final int DEFAULT_ACCOUNT_TYPE = ModelConstants.DEFAULT_ACCOUNT_TYPE;        // Расчетный счет по умолчанию
    public static final int DEFAULT_CURRENCY_ID = ModelConstants.DEFAULT_CURRENCY_ID;          // ID валюты по умолчанию (рубли)
    public static final int DEFAULT_ACCOUNT_STATUS_OPEN = ModelConstants.ACCOUNT_STATUS_OPEN;       // Счет не закрыт по умолчанию

    // Значения по умолчанию для категорий (ссылаемся на ModelConstants)
    public static final int DEFAULT_CATEGORY_OPERATION_TYPE = ModelConstants.DEFAULT_CATEGORY_OPERATION_TYPE;  // Тип операции по умолчанию
    public static final int DEFAULT_CATEGORY_TYPE = ModelConstants.DEFAULT_CATEGORY_TYPE;                      // Тип категории по умолчанию
    public static final Integer DEFAULT_PARENT_CATEGORY_ID = ModelConstants.DEFAULT_PARENT_ID;          // ID родителя по умолчанию (корневая категория)

    // ========================================
    // СООБЩЕНИЯ ДЛЯ OPERATION SERVICE
    // ========================================
    
    // Создание операции
    public static final String MSG_CREATE_OPERATION_REQUEST = "Запрос на создание операции";
    public static final String MSG_CREATE_OPERATION_SUCCESS = "Операция успешно создана";
    public static final String MSG_CREATE_OPERATION_ERROR = "Ошибка при создании операции: ";
    
    // Удаление операции
    public static final String MSG_DELETE_OPERATION_NOT_FOUND = "Операция не найдена для удаления. Удаление было отменено";
    public static final String MSG_DELETE_OPERATION_REQUEST = "Запрос на удаление операции: ";
    public static final String MSG_DELETE_OPERATION_SUCCESS = "Операция успешно удалена";
    public static final String MSG_DELETE_OPERATION_ERROR = "Ошибка при удалении операции '";
    
    // Восстановление операции
    public static final String MSG_RESTORE_OPERATION_NOT_FOUND = "Операция не найдена для восстановления. Восстановление было отменено";
    public static final String MSG_RESTORE_OPERATION_REQUEST = "Запрос на восстановление операции ";
    public static final String MSG_RESTORE_OPERATION_SUCCESS = "Операция успешно восстановлена";
    public static final String MSG_RESTORE_OPERATION_ERROR = "Ошибка при восстановлении операции '";
    
    // Soft delete операции
    public static final String MSG_SOFT_DELETE_OPERATION_REQUEST = "Запрос на softDelete операции ";
    public static final String MSG_SOFT_DELETE_OPERATION_SUCCESS = "Операция успешно soft deleted";
    public static final String MSG_SOFT_DELETE_OPERATION_ERROR = "Ошибка при soft delete операции '";
    
    // Обновление операции
    public static final String MSG_UPDATE_OPERATION_NOT_FOUND = "Операция не найдена для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_OPERATION_REQUEST = "Запрос на обновление операции ";
    public static final String MSG_UPDATE_OPERATION_SUCCESS = "Операция успешно обновлена";
    public static final String MSG_UPDATE_OPERATION_ERROR = "Ошибка при обновлении операции ";
    
    // Тексты ошибок и сообщений
    
}