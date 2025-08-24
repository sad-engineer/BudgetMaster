package com.sadengineer.budgetmaster.backend.constants;

import java.time.LocalDateTime;

/**
 * Константы для моделей
 */
public class ModelConstants {
    
    // ========================================
    // ОБЩИЕ КОНСТАНТЫ
    // ========================================
    
    // Позиции
    public static final int DEFAULT_POSITION = 1;   // По умолчанию позиция 1
    public static final int MIN_POSITION = 1;       // Минимальная позиция 1    
    public static final int MAX_POSITION = Integer.MAX_VALUE; // Максимальная позиция Integer.MAX_VALUE
    
    // Суммы
    public static final long DEFAULT_AMOUNT = 0;       // Сумма по умолчанию 0
    public static final long MIN_AMOUNT = 0;         // Минимальная сумма 0
    public static final long MAX_AMOUNT = Long.MAX_VALUE; // Максимальная сумма 9,223,372,036,854,775,807 (9 квинтиллионов)
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ ВАЛЮТ (CURRENCY)
    // ========================================
    
    // Значения по умолчанию для валют
    public static final int DEFAULT_CURRENCY_ID = 1;  // Рубли по умолчанию
    public static final String DEFAULT_CURRENCY_TITLE = "Рубль"; // Название валюты по умолчанию
    public static final String DEFAULT_CURRENCY_SHORT_NAME = "₽"; // Короткое имя валюты по умолчанию

    // ========================================
    // КОНСТАНТЫ ДЛЯ СЧЕТОВ (ACCOUNT)
    // ========================================
    
    // Типы счетов
    public static final int ACCOUNT_TYPE_CURRENT = 1;   // Расчетный счет
    public static final int ACCOUNT_TYPE_SAVINGS = 2;   // Сберегательный счет
    public static final int ACCOUNT_TYPE_CREDIT = 3;    // Кредитный счет
    
    // Статусы закрытия счетов
    public static final int ACCOUNT_STATUS_OPEN = 0;    // Счет открыт
    public static final int ACCOUNT_STATUS_CLOSED = 1;  // Счет закрыт
    
    // Значения по умолчанию для счетов
    public static final int DEFAULT_ACCOUNT_TYPE = ACCOUNT_TYPE_CURRENT; // Расчетный счет по умолчанию
    public static final long DEFAULT_ACCOUNT_BALANCE = DEFAULT_AMOUNT; // Баланс счета по умолчанию
    public static final int DEFAULT_ACCOUNT_STATUS_OPEN = ACCOUNT_STATUS_OPEN; // Счет открыт по умолчанию
    
    // Значения по умолчанию для кредитных карт
    public static final long DEFAULT_CREDIT_CARD_LIMIT = DEFAULT_AMOUNT; // Лимит кредитной карты по умолчанию
    public static final long DEFAULT_CREDIT_CARD_CATEGORY_ID = DEFAULT_AMOUNT; // Категория кредитной карты по умолчанию
    public static final long DEFAULT_CREDIT_CARD_COMMISSION_CATEGORY_ID = DEFAULT_AMOUNT; // Категория комиссии кредитной карты по умолчанию
    
    // Значения по умолчанию для счетов
    public static final int DEFAULT_ACCOUNT_ID = 1; // ID счета по умолчанию
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ КАТЕГОРИЙ (CATEGORY)
    // ========================================
    
    // Типы категорий
    public static final int CATEGORY_TYPE_PARENT = 0;   // Родительская категория
    public static final int CATEGORY_TYPE_CHILD = 1;    // Дочерняя категория
    
    // Значения по умолчанию для категорий
    public static final String DEFAULT_CATEGORY_TITLE = "Новая категория"; // Название категории по умолчанию
    public static final int DEFAULT_CATEGORY_OPERATION_TYPE = 1; // Тип операции по умолчанию (без ссылки, а то реверсивная ошибка)
    public static final int DEFAULT_CATEGORY_TYPE = CATEGORY_TYPE_PARENT; // Тип категории по умолчанию (родительская)
    public static final Integer DEFAULT_PARENT_ID = null; // ID категории по умолчанию (нет для родительской категории)
    public static final int DEFAULT_CATEGORY_ID = 1; // ID категории по умолчанию
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ БЮДЖЕТОВ (BUDGET)
    // ========================================
    
    // Значения по умолчанию для бюджетов
    public static final long DEFAULT_BUDGET_AMOUNT = DEFAULT_AMOUNT; // Бюджет по умолчанию 0
    public static final int DEFAULT_BUDGET_CURRENCY_ID = DEFAULT_CURRENCY_ID; // Валюта бюджета по умолчанию
    public static final int DEFAULT_BUDGET_CATEGORY_ID = DEFAULT_CATEGORY_ID; // Категория бюджета по умолчанию
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ ОПЕРАЦИЙ (OPERATION)
    // ========================================
    
    // Типы операций
    public static final int OPERATION_TYPE_EXPENSE = 1; // Расход
    public static final int OPERATION_TYPE_INCOME = 2;  // Доход
    
    // Значения по умолчанию для операций
    public static final int DEFAULT_OPERATION_ACCOUNT_ID = DEFAULT_ACCOUNT_ID; // ID счета по умолчанию
    public static final int DEFAULT_OPERATION_CATEGORY_ID = DEFAULT_CATEGORY_ID; // ID категории по умолчанию
    public static final long DEFAULT_OPERATION_AMOUNT = DEFAULT_AMOUNT; // Сумма операции по умолчанию
    public static final String DEFAULT_OPERATION_DESCRIPTION = "Новая операция"; // Описание операции по умолчанию
    public static final LocalDateTime DEFAULT_OPERATION_OPERATION_DATE = LocalDateTime.now(); // Дата операции по умолчанию
    public static final int DEFAULT_OPERATION_TYPE = OPERATION_TYPE_EXPENSE; // Тип операции по умолчанию
    public static final int DEFAULT_OPERATION_CURRENCY_ID = DEFAULT_CURRENCY_ID; // ID валюты операции по умолчанию
    
    // Значения по умолчанию для переводов
    public static final long DEFAULT_TO_ACCOUNT_ID = DEFAULT_ACCOUNT_ID; // Счет для перевода по умолчанию
    public static final long DEFAULT_TO_CURRENCY_ID = DEFAULT_CURRENCY_ID; // Валюта для перевода по умолчанию
    public static final long DEFAULT_TO_AMOUNT = DEFAULT_AMOUNT; // Сумма для перевода по умолчанию
    
    // Флаги для операций
    public static final boolean IS_TRANSFER = true; // Перевод
    public static final boolean IS_NOT_TRANSFER = false; // Не перевод
    
} 