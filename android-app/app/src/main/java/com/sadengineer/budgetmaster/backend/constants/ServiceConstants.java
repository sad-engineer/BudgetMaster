
package com.sadengineer.budgetmaster.backend.constants;

/**
 * Константы для сервисов
 */
public class ServiceConstants {
    
    // База данных (ссылаемся на RepositoryConstants)
    public static final String DEFAULT_DATABASE_NAME = RepositoryConstants.DATABASE_PATH;
    
    // Значения по умолчанию для счетов (ссылаемся на ModelConstants)
    public static final int DEFAULT_ACCOUNT_BALANCE = ModelConstants.DEFAULT_ACCOUNT_BALANCE;  // Начальный баланс по умолчанию
    public static final int DEFAULT_ACCOUNT_TYPE = ModelConstants.DEFAULT_ACCOUNT_TYPE;        // Расчетный счет по умолчанию
    public static final int DEFAULT_CURRENCY_ID = ModelConstants.DEFAULT_CURRENCY_ID;          // ID валюты по умолчанию (рубли)
    public static final int DEFAULT_ACCOUNT_CLOSED = ModelConstants.ACCOUNT_STATUS_OPEN;       // Счет не закрыт по умолчанию
    
    // Сообщения об ошибках для сервисов
    public static final String ERROR_POSITION_OUT_OF_RANGE = "Новая позиция должна быть от 1 до ";
    public static final String ERROR_ENTITY_ALREADY_EXISTS = " уже существует";
    public static final String ERROR_CANNOT_UPDATE_DELETED = "Нельзя обновить удаленную ";
    public static final String ERROR_ENTITY_NOT_FOUND = " не найдена";
    public static final String ERROR_ENTITY_NOT_FOUND_BY_ID = " с ID ";
    
    // Специфичные сообщения для валют
    public static final String ERROR_CURRENCY_ALREADY_EXISTS = "Валюта с названием '";
    public static final String ERROR_CANNOT_UPDATE_DELETED_CURRENCY = "Нельзя обновить удаленную валюту";
    public static final String ERROR_CURRENCY_NOT_FOUND = "Валюта с ID ";
    
    // Специфичные сообщения для счетов
    public static final String ERROR_ACCOUNT_ALREADY_EXISTS = "Счет с названием '";
    public static final String ERROR_CANNOT_UPDATE_DELETED_ACCOUNT = "Нельзя обновить удаленный счет";
    public static final String ERROR_ACCOUNT_NOT_FOUND = "Счет с ID ";
    
    // Специфичные сообщения для категорий
    public static final String ERROR_CATEGORY_ALREADY_EXISTS = "Категория с названием '";
    public static final String ERROR_CANNOT_UPDATE_DELETED_CATEGORY = "Нельзя обновить удаленную категорию";
    public static final String ERROR_CATEGORY_NOT_FOUND = "Категория с ID ";
    
    // Специфичные сообщения для бюджетов
    public static final String ERROR_BUDGET_ALREADY_EXISTS = "Бюджет с названием '";
    public static final String ERROR_CANNOT_UPDATE_DELETED_BUDGET = "Нельзя обновить удаленный бюджет";
    public static final String ERROR_BUDGET_NOT_FOUND = "Бюджет с ID ";
    
    // Специфичные сообщения для операций
    public static final String ERROR_OPERATION_ALREADY_EXISTS = "Операция с ID '";
    public static final String ERROR_CANNOT_UPDATE_DELETED_OPERATION = "Нельзя обновить удаленную операцию";
    public static final String ERROR_OPERATION_NOT_FOUND = "Операция с ID ";
    
    // Сообщения для смены пользователя
    public static final String ERROR_CANNOT_CHANGE_USER = "Для смены пользователя создайте новый экземпляр ";
    
    // Комментарии для значений по умолчанию
    public static final String COMMENT_DEFAULT_ACCOUNT = "Расчетный счет по умолчанию, ID валюты по умолчанию (рубли), не закрытый";
} 