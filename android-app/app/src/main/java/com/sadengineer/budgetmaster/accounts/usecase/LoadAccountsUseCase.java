package com.sadengineer.budgetmaster.accounts.usecase;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.service.AccountService;

import java.util.List;

/**
 * Use Case для загрузки счетов по типу
 */
public class LoadAccountsUseCase implements BaseUseCaseWithParams<LoadAccountsUseCase.Params, LiveData<List<Account>>> {
    
    private final AccountService accountService;
    
    public LoadAccountsUseCase(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public LiveData<List<Account>> execute(Params params) {
        return accountService.getAllByType(params.accountType.getIndex(), params.filter);
    }
    
    /**
     * Параметры для загрузки счетов
     */
    public static class Params {
        public final AccountTypeFilter accountType;
        public final EntityFilter filter;
        
        public Params(AccountTypeFilter accountType, EntityFilter filter) {
            this.accountType = accountType;
            this.filter = filter;
        }
        
        /**
         * Создает параметры для загрузки активных счетов
         */
        public static Params forActiveAccounts(AccountTypeFilter accountType) {
            return new Params(accountType, EntityFilter.ACTIVE);
        }
        
        /**
         * Создает параметры для загрузки всех счетов
         */
        public static Params forAllAccounts(AccountTypeFilter accountType) {
            return new Params(accountType, EntityFilter.ALL);
        }
    }
}
