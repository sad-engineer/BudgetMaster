package com.sadengineer.budgetmaster.backend.filters;

import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Фильтр типов счетов
 */
public enum AccountTypeFilter {

    /**
     * Только расчетные счета
     */
    CURRENT(ModelConstants.ACCOUNT_TYPE_CURRENT),
    
    /**
     * Только сберегательные счета
     */
    SAVINGS(ModelConstants.ACCOUNT_TYPE_SAVINGS),

    /**
     * Только кредитные счета
     */
    CREDIT(ModelConstants.ACCOUNT_TYPE_CREDIT),

    /**
     * Все типы счетов
     */
    ALL(null);

    private final Integer accountType;

    AccountTypeFilter(Integer accountType) {
        this.accountType = accountType;
    }

    /**
     * Получить значение типа счета
     * @return значение типа счета или null для ALL
     */
    public Integer getIndex() {
        return accountType;
    }
}
