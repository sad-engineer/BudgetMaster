package com.sadengineer.budgetmaster.backend.filters;

import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

/**
 * Фильтр статусов счетов
 */
public enum AccountStatusFilter {

    /**
     * Только открытые счета
     */
    OPEN(ModelConstants.ACCOUNT_STATUS_OPEN),
    
    /**
     * Только закрытые счета
     */
    CLOSED(ModelConstants.ACCOUNT_STATUS_CLOSED),

    /**
     * Все статусы счетов
     */
    ALL(null);

    private final Integer accountStatus;

    AccountStatusFilter(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * Получить значение статуса счета
     * @return значение статуса счета или null для ALL
     */
    public Integer getIndex() {
        return accountStatus;
    }
}
