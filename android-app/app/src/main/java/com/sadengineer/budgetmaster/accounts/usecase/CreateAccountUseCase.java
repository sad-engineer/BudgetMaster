package com.sadengineer.budgetmaster.accounts.usecase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.concurrent.CompletableFuture;

/**
 * Use Case для создания нового счета
 */
public class CreateAccountUseCase implements AsyncUseCaseWithParams<CreateAccountUseCase.Params, Boolean> {
    
    private static final String TAG = "CreateAccountUseCase";
    private final AccountService accountService;
    
    public CreateAccountUseCase(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public CompletableFuture<Boolean> executeAsync(Params params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                accountService.createWithoutValidation(
                    params.title,
                    params.currencyId,
                    params.amount,
                    params.type,
                    params.closed
                );
                
                LogManager.d(TAG, "Счет создан: " + params.title);
                return true;
                
            } catch (Exception e) {
                LogManager.e(TAG, "Ошибка создания счета: " + e.getMessage(), e);
                return false;
            }
        });
    }    
    
    /**
     * Параметры для создания счета
     */
    public static class Params {
        public final String title;
        public final int currencyId;
        public final long amount;
        public final int type;
        public final int closed;
        
        public Params(String title, int currencyId, long amount, int type, int closed) {
            this.title = title;
            this.currencyId = currencyId;
            this.amount = amount;
            this.type = type;
            this.closed = closed;
        }
        
        /**
         * Создает параметры из объекта Account
         */
        public static Params fromAccount(Account account) {
            return new Params(
                account.getTitle(),
                account.getCurrencyId(),
                account.getAmount(),
                account.getType(),
                account.getClosed()
            );
        }
    }
}
