package com.sadengineer.budgetmaster.backend.entity;

/**
 * Фильтр типов операций бюджетов
 */
public enum OperationTypeFilter {
    /**
     * Только активные бюджеты для расходов (operation_type = 1)
     */
    EXPENSE,
    
    /**
     * Только активные бюджеты для доходов (operation_type = 2)
     */
    INCOME,

    /**
     * Все бюджеты (включая активные для расходов и доходов)
     */
    ALL
} 


