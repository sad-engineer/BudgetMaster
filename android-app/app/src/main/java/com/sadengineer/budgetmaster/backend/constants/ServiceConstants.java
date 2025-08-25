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
    public static final String MSG_RESTORE_OPERATION_ERROR = "Ошибка при восстановлении операции %s: ";
    
    // Soft delete операции
    public static final String MSG_SOFT_DELETE_OPERATION_REQUEST = "Запрос на softDelete операции ";
    public static final String MSG_SOFT_DELETE_OPERATION_SUCCESS = "Операция успешно soft deleted";
    public static final String MSG_SOFT_DELETE_OPERATION_ERROR = "Ошибка при soft delete операции %s: ";
    
    // Обновление операции
    public static final String MSG_UPDATE_OPERATION_NOT_FOUND = "Операция не найдена для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_OPERATION_REQUEST = "Запрос на обновление операции ";
    public static final String MSG_UPDATE_OPERATION_SUCCESS = "Операция успешно обновлена";
    public static final String MSG_UPDATE_OPERATION_ERROR = "Ошибка при обновлении операции %s: ";
    
    // Тексты ошибок и сообщений
    
    // ========================================
    // СООБЩЕНИЯ ДЛЯ ACCOUNT SERVICE
    // ========================================
    
    // Создание счета
    public static final String MSG_CREATE_ACCOUNT_REQUEST = "Запрос на создание счета: %s";
    public static final String MSG_CREATE_ACCOUNT_SUCCESS = " успешно создан";
    public static final String MSG_CREATE_ACCOUNT_ERROR = "Ошибка при создании счета %s: ";
    public static final String MSG_ACCOUNT_CREATED = "Счет %s успешно создан";
    
    // Удаление счета
    public static final String MSG_DELETE_ACCOUNT_NOT_FOUND = "Счет не найден для удаления. Удаление было отменено";
    public static final String MSG_DELETE_ACCOUNT_REQUEST = "Запрос на удаление счета: %s";
    public static final String MSG_DELETE_ACCOUNT_SUCCESS = " успешно удален";
    public static final String MSG_DELETE_ACCOUNT_ERROR = "Ошибка при удаления счета %s: ";
    public static final String MSG_ACCOUNT_DELETED = "Счет %s успешно удален";
    
    // Восстановление счета
    public static final String MSG_RESTORE_ACCOUNT_REQUEST = "Запрос на восстановление счета для категории %s";
    public static final String MSG_RESTORE_ACCOUNT_SUCCESS = " успешно восстановлен";
    public static final String MSG_RESTORE_ACCOUNT_ERROR = "Ошибка при восстановлении счета %s: ";
    public static final String MSG_ACCOUNT_RESTORED = "Счет %s успешно восстановлен";
    
    // Soft delete счета
    public static final String MSG_SOFT_DELETE_ACCOUNT_REQUEST = "Запрос на softDelete счета для категории %s";
    public static final String MSG_SOFT_DELETE_ACCOUNT_SUCCESS = " успешно soft deleted";
    public static final String MSG_SOFT_DELETE_ACCOUNT_ERROR = "Ошибка при soft delete счета %s: ";
    public static final String MSG_ACCOUNT_SOFT_DELETED = "Счет %s успешно soft deleted";
    
    // Обновление счета
    public static final String MSG_UPDATE_ACCOUNT_NOT_FOUND = "Счет не найден для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_ACCOUNT_REQUEST = "Запрос на обновление счета для категории %s";
    public static final String MSG_UPDATE_ACCOUNT_SUCCESS = " успешно отправлен";
    public static final String MSG_UPDATE_ACCOUNT_ERROR = "Ошибка при обновлении счета для категории %s: ";
    public static final String MSG_ACCOUNT_UPDATED = "Запрос на обновление счета для категории %s успешно отправлен";
    
    // ========================================
    // СООБЩЕНИЯ ДЛЯ BUDGET SERVICE
    // ========================================
    
    // Создание бюджета
    public static final String MSG_CREATE_BUDGET_REQUEST = "Запрос на создание бюджета для категории %s";
    public static final String MSG_CREATE_BUDGET_SUCCESS = " успешно создан";
    public static final String MSG_CREATE_BUDGET_ERROR = "Ошибка при создании бюджета для категории %s: ";
    public static final String MSG_BUDGET_CREATED = "Бюджет для категории %s успешно создан";
    
    // Удаление бюджета
    public static final String MSG_DELETE_BUDGET_NOT_FOUND = "Бюджет не найден для удаления. Удаление было отменено";
    public static final String MSG_DELETE_BUDGET_REQUEST = "Запрос на удаление бюджета для категории %s";
    public static final String MSG_DELETE_BUDGET_SUCCESS = " успешно удален";
    public static final String MSG_DELETE_BUDGET_ERROR = "Ошибка при удалении бюджета для категории %s: ";
    public static final String MSG_BUDGET_DELETED = "Бюджет для категории %s успешно удален";
    
    // Восстановление бюджета
    public static final String MSG_RESTORE_BUDGET_NOT_FOUND = "Бюджет не найден для восстановления";
    public static final String MSG_RESTORE_BUDGET_REQUEST = "Запрос на восстановление бюджета для категории %s";
    public static final String MSG_RESTORE_BUDGET_SUCCESS = " успешно восстановлен";
    public static final String MSG_RESTORE_BUDGET_ERROR = "Ошибка при восстановлении бюджета для категории %s: ";
    public static final String MSG_BUDGET_RESTORED = "Бюджет для категории %s успешно восстановлен";
    
    // Soft delete бюджета
    public static final String MSG_SOFT_DELETE_BUDGET_REQUEST = "Запрос на softDelete бюджета для категории %s";
    public static final String MSG_SOFT_DELETE_BUDGET_SUCCESS = " успешно soft deleted";
    public static final String MSG_SOFT_DELETE_BUDGET_ERROR = "Ошибка при soft delete бюджета для категории %s: ";
    public static final String MSG_BUDGET_SOFT_DELETED = "Бюджет для категории %s успешно soft deleted";
    
    // Обновление бюджета
    public static final String MSG_UPDATE_BUDGET_NOT_FOUND = "Бюджет не найден для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_BUDGET_REQUEST = "Запрос на обновление бюджета для категории %s";
    public static final String MSG_UPDATE_BUDGET_SUCCESS = " успешно отправлен";
    public static final String MSG_UPDATE_BUDGET_ERROR = "Ошибка при обновлении бюджета для категории %s: ";
    public static final String MSG_BUDGET_UPDATED = "Запрос на обновление бюджета для категории %s успешно отправлен";
    
    // ========================================
    // СООБЩЕНИЯ ДЛЯ CATEGORY SERVICE
    // ========================================
    
    // Создание категории
    public static final String MSG_CREATE_CATEGORY_REQUEST = "Запрос на создание категории: %s";
    public static final String MSG_CREATE_CATEGORY_SUCCESS = " успешно создана";
    public static final String MSG_CREATE_CATEGORY_ERROR = "Ошибка при создании категории '%s': ";
    public static final String MSG_CATEGORY_CREATED = "Категория %s успешно создана";
    
    // Удаление категории
    public static final String MSG_DELETE_CATEGORY_NOT_FOUND = "Категория не найдена для удаления. Удаление было отменено";
    public static final String MSG_DELETE_CATEGORY_REQUEST = "Запрос на удаление категории: %s";
    public static final String MSG_DELETE_CATEGORY_SUCCESS = " успешно удалена";
    public static final String MSG_DELETE_CATEGORY_ERROR = "Ошибка при удалении категории '%s': ";
    public static final String MSG_CATEGORY_DELETED = "Категория %s успешно удалена";
    
    // Восстановление категории
    public static final String MSG_RESTORE_CATEGORY_NOT_FOUND = "Категория не найдена для восстановления. Восстановление было отменено";
    public static final String MSG_RESTORE_CATEGORY_REQUEST = "Запрос на восстановление категории: %s";
    public static final String MSG_RESTORE_CATEGORY_SUCCESS = " успешно восстановлена";
    public static final String MSG_RESTORE_CATEGORY_ERROR = "Ошибка при восстановлении категории '%s': ";
    public static final String MSG_CATEGORY_RESTORED = "Категория %s успешно восстановлена";
    
    // Soft delete категории
    public static final String MSG_SOFT_DELETE_CATEGORY_REQUEST = "Запрос на softDelete категории %s";
    public static final String MSG_SOFT_DELETE_CATEGORY_SUCCESS = " успешно soft deleted";
    public static final String MSG_SOFT_DELETE_CATEGORY_ERROR = "Ошибка при soft delete категории '%s': ";
    public static final String MSG_CATEGORY_SOFT_DELETED = "Категория %s успешно soft deleted";
    
    // Обновление категории
    public static final String MSG_UPDATE_CATEGORY_NOT_FOUND = "Категория не найдена для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_CATEGORY_REQUEST = "Запрос на обновление категории %s";
    public static final String MSG_UPDATE_CATEGORY_SUCCESS = " успешно обновлена";
    public static final String MSG_UPDATE_CATEGORY_ERROR = "Ошибка при обновлении категории '%s': ";
    public static final String MSG_CATEGORY_UPDATED = "Категория %s успешно обновлена";
    
    // ========================================
    // СООБЩЕНИЯ ДЛЯ CURRENCY SERVICE
    // ========================================
    
    // Создание валюты
    public static final String MSG_CREATE_CURRENCY_REQUEST = "Запрос на создание валюты: %s";
    public static final String MSG_CREATE_CURRENCY_SUCCESS = " успешно создана";
    public static final String MSG_CREATE_CURRENCY_ERROR = "Ошибка при создании валюты '%s': ";
    public static final String MSG_CURRENCY_CREATED = "Валюта %s успешно создана";
    
    // Удаление валюты
    public static final String MSG_DELETE_CURRENCY_NOT_FOUND = "Валюта не передана для удаления. Удаление было отменено";
    public static final String MSG_DELETE_CURRENCY_REQUEST = "Запрос на удаление валюты: %s";
    public static final String MSG_DELETE_CURRENCY_SUCCESS = " успешно удалена";
    public static final String MSG_DELETE_CURRENCY_ERROR = "Ошибка при удалении валюты '%s': ";
    public static final String MSG_CURRENCY_DELETED = "Валюта %s успешно удалена";
    
    // Восстановление валюты
    public static final String MSG_RESTORE_CURRENCY_NOT_FOUND = "Валюта не передана для восстановления. Восстановление было отменено";
    public static final String MSG_RESTORE_CURRENCY_REQUEST = "Запрос на восстановление валюты %s";
    public static final String MSG_RESTORE_CURRENCY_SUCCESS = " успешно восстановлена";
    public static final String MSG_RESTORE_CURRENCY_ERROR = "Ошибка при восстановлении валюты '%s': ";
    public static final String MSG_CURRENCY_RESTORED = "Валюта %s успешно восстановлена";
    
    // Soft delete валюты
    public static final String MSG_SOFT_DELETE_CURRENCY_NOT_FOUND = "Валюта не найдена для soft delete. Удаление было отменено";
    public static final String MSG_SOFT_DELETE_CURRENCY_REQUEST = "Запрос на softDelete валюты %s";
    public static final String MSG_SOFT_DELETE_CURRENCY_SUCCESS = " успешно soft deleted";
    public static final String MSG_SOFT_DELETE_CURRENCY_ERROR = "Ошибка при soft delete валюты '%s': ";
    public static final String MSG_CURRENCY_SOFT_DELETED = "Валюта %s успешно soft deleted";
    
    // Обновление валюты
    public static final String MSG_UPDATE_CURRENCY_NOT_FOUND = "Валюта не найдена для обновления. Обновление было отменено";
    public static final String MSG_UPDATE_CURRENCY_REQUEST = "Запрос на обновление валюты %s";
    public static final String MSG_UPDATE_CURRENCY_SUCCESS = " успешно отправлен";
    public static final String MSG_UPDATE_CURRENCY_ERROR = "Ошибка при обновлении валюты для категории %s: ";
    public static final String MSG_CURRENCY_UPDATED = "Запрос на обновление валюты для категории %s успешно отправлен";
}