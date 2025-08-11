// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.constants;

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
    public static final int DEFAULT_AMOUNT = 0;       // Сумма по умолчанию 0
    public static final int MIN_AMOUNT = 0;         // Минимальная сумма 0
    public static final int MAX_AMOUNT = Integer.MAX_VALUE; // Максимальная сумма Integer.MAX_VALUE
    
    // Специальные значения
    public static final int INVALID_ID = -1; // Некорректный ID 
    public static final int ROOT_CATEGORY_ID = 0;  // ID корневой категории (родительская)
    
    // Статусы сущностей
    public static final boolean ENTITY_ACTIVE = true; // Статус активной сущности
    public static final boolean ENTITY_DELETED = false; // Статус удаленной сущности
    
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
    public static final int DEFAULT_ACCOUNT_BALANCE = DEFAULT_AMOUNT; // Баланс счета по умолчанию
    public static final int DEFAULT_ACCOUNT_STATUS_OPEN = ACCOUNT_STATUS_OPEN; // Счет открыт по умолчанию
    
    // Значения по умолчанию для кредитных карт
    public static final Integer DEFAULT_CREDIT_CARD_LIMIT = null; // Лимит кредитной карты по умолчанию
    public static final Integer DEFAULT_CREDIT_CARD_CATEGORY_ID = null; // Категория кредитной карты по умолчанию
    public static final Integer DEFAULT_CREDIT_CARD_COMMISSION_CATEGORY_ID = null; // Категория комиссии кредитной карты по умолчанию
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ КАТЕГОРИЙ (CATEGORY)
    // ========================================
    
    // Типы категорий
    public static final int CATEGORY_TYPE_PARENT = 0;   // Родительская категория
    public static final int CATEGORY_TYPE_CHILD = 1;    // Дочерняя категория
    
    // Значения по умолчанию для категорий
    public static final int DEFAULT_CATEGORY_OPERATION_TYPE = OPERATION_TYPE_EXPENSE; // Расход по умолчанию
    public static final int DEFAULT_CATEGORY_TYPE = CATEGORY_TYPE_CHILD; // Дочерняя категория по умолчанию
    public static final int DEFAULT_PARENT_CATEGORY_ID = ROOT_CATEGORY_ID; // ID родителя по умолчанию (корневая категория)
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ ОПЕРАЦИЙ (OPERATION)
    // ========================================
    
    // Типы операций
    public static final int OPERATION_TYPE_EXPENSE = 1; // Расход
    public static final int OPERATION_TYPE_INCOME = 2;  // Доход
    
    // Значения по умолчанию для операций
    public static final int DEFAULT_OPERATION_TYPE = 1; // Расход по умолчанию
    
    // Значения по умолчанию для переводов
    public static final Integer DEFAULT_TO_ACCOUNT_ID = null; // Счет для перевода по умолчанию
    public static final Integer DEFAULT_TO_CURRENCY_ID = null; // Валюта для перевода по умолчанию
    public static final Integer DEFAULT_TO_AMOUNT = null; // Сумма для перевода по умолчанию
    
    // Флаги для операций
    public static final boolean IS_TRANSFER = true; // Перевод
    public static final boolean IS_NOT_TRANSFER = false; // Не перевод
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ БЮДЖЕТОВ (BUDGET)
    // ========================================
    
    // Значения по умолчанию для бюджетов
    public static final int DEFAULT_BUDGET_AMOUNT = 0; // Бюджет по умолчанию 0
    public static final Integer DEFAULT_BUDGET_CATEGORY_ID = null; // Категория бюджета по умолчанию
    
    // ========================================
    // КОНСТАНТЫ ДЛЯ ВАЛЮТ (CURRENCY)
    // ========================================
    
    // Значения по умолчанию для валют
    public static final int DEFAULT_CURRENCY_ID = 1;  // Рубли по умолчанию
} 