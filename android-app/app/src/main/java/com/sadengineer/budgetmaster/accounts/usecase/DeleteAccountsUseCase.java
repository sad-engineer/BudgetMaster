package com.sadengineer.budgetmaster.accounts.usecase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Use Case для мягкого удаления счетов
 */
public class DeleteAccountsUseCase implements AsyncUseCaseWithParams<List<Account>, Integer> {
    
    private static final String TAG = "DeleteAccountsUseCase";
    private final AccountService accountService;
    
    public DeleteAccountsUseCase(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @Override
    public CompletableFuture<Integer> executeAsync(List<Account> accounts) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (accounts == null || accounts.isEmpty()) {
                    LogManager.d(TAG, "Нет счетов для удаления");
                    return 0;
                }
                
                List<Account> accountsToDelete = accounts.stream()
                    .filter(account -> account != null && !account.isDeleted())
                    .collect(Collectors.toList());
                
                if (accountsToDelete.isEmpty()) {
                    LogManager.w(TAG, "Нет счетов для удаления - все уже удалены");
                    return 0;
                }
                
                LogManager.d(TAG, "Начато удаление счетов. Количество: " + accountsToDelete.size());
                
                int deletedCount = 0;
                for (Account account : accountsToDelete) {
                    try {
                        LogManager.d(TAG, "Удаление счёта: ID=" + account.getId());
                        accountService.delete(account, true); // Soft delete
                        deletedCount++;
                        LogManager.d(TAG, "Счет ID: " + account.getId() + " успешно удален");
                    } catch (Exception e) {
                        LogManager.e(TAG, "Ошибка удаления счёта: ID=" + account.getId() + ", причина: " + e.getMessage());
                    }
                }
                
                LogManager.d(TAG, "Удаление завершено. Удалено счетов: " + deletedCount);
                return deletedCount;
                
            } catch (Exception e) {
                LogManager.e(TAG, "Критическая ошибка при удалении счетов: " + e.getMessage(), e);
                return 0;
            }
        });
    }
}
