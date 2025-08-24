package com.sadengineer.budgetmaster.backend.filters;

import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Фильтр типов операций
 */
public enum OperationTypeFilter {

    /**
     * Только доходы
     */
    INCOME(ModelConstants.OPERATION_TYPE_INCOME),
    
    /**
     * Только расходы
     */
    EXPENSE(ModelConstants.OPERATION_TYPE_EXPENSE),

    /**
     * Все типы операций
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


