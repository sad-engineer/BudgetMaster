package com.sadengineer.budgetmaster.accounts.usecase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.concurrent.CompletableFuture;

/**
 * Use Case для обновления существующего счета
 */
public class UpdateAccountUseCase implements AsyncUseCaseWithParams<Account, Boolean> {
    
    private static final String TAG = "UpdateAccountUseCase";
    private final AccountService accountService;
    
    public UpdateAccountUseCase(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public CompletableFuture<Boolean> executeAsync(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                accountService.update(account);
                
                LogManager.d(TAG, "Счет обновлен: " + account.getTitle());
                return true;
                
            } catch (Exception e) {
                LogManager.e(TAG, "Ошибка обновления счета: " + e.getMessage(), e);
                return false;
            }
        });
    }
}
