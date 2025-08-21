package com.sadengineer.budgetmaster.backend.entity;

import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Фильтр типов операций бюджетов
 */
public enum OperationTypeFilter {
    /**
     * Только бюджеты для расходов (operation_type = 1)
     */
    EXPENSE(ModelConstants.OPERATION_TYPE_EXPENSE),
    
    /**
     * Только  бюджеты для доходов (operation_type = 2)
     */
    INCOME(ModelConstants.OPERATION_TYPE_INCOME),

    /**
     * Все бюджеты (для расходов и доходов)
     */
    ALL(null);

    private final Integer operationType;

    OperationTypeFilter(Integer operationType) {
        this.operationType = operationType;
    }

    /**
     * Получить значение типа операции
     * @return значение типа операции или null для ALL
     */
    public Integer getIndex() {
        return operationType;
    }
} 


