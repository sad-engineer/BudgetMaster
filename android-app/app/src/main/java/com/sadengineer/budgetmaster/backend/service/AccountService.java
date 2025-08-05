
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Account
 */
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final ExecutorService executorService;
    private final String user;
    
    public AccountService(Context context, String user) {
        this.accountRepository = new AccountRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }
    
    // Получить все активные счета
    public LiveData<List<Account>> getAllActiveAccounts() {
        return accountRepository.getAllActiveAccounts();
    }
    
    // Получить текущие счета
    public LiveData<List<Account>> getCurrentAccounts() {
        return accountRepository.getAccountsByType("current");
    }
    
    // Получить сберегательные счета
    public LiveData<List<Account>> getSavingsAccounts() {
        return accountRepository.getAccountsByType("savings");
    }
    
    // Получить кредитные счета
    public LiveData<List<Account>> getCreditAccounts() {
        return accountRepository.getAccountsByType("credit");
    }
    
    // Получить счет по ID
    public LiveData<Account> getAccountById(int id) {
        return accountRepository.getAccountById(id);
    }
    
    // Получить счет по названию
    public LiveData<Account> getAccountByTitle(String title) {
        return accountRepository.getAccountByTitle(title);
    }
    
    // Получить счета по валюте
    public LiveData<List<Account>> getAccountsByCurrency(int currencyId) {
        return accountRepository.getAccountsByCurrency(currencyId);
    }
    
    // Получить общий баланс по валюте
    public LiveData<Integer> getTotalBalanceByCurrency(int currencyId) {
        return accountRepository.getTotalBalanceByCurrency(currencyId);
    }
    
    // Создать новый счет
    public void createAccount(String title, int currencyId, int amount, int type) {
        Account account = new Account();
        account.setTitle(title);
        account.setAmount(amount);
        account.setType(type);
        account.setCurrencyId(currencyId);
        account.setClosed(0);
        account.setPosition(1); // TODO: Получить следующую позицию
        accountRepository.insertAccount(account, user);
    }
    
    // Обновить счет
    public void updateAccount(Account account) {
        accountRepository.updateAccount(account, user);
    }
    
    // Удалить счет (soft delete)
    public void deleteAccount(int accountId) {
        accountRepository.deleteAccount(accountId, user);
    }
    
    // Восстановить удаленный счет
    public void restoreAccount(int accountId) {
        executorService.execute(() -> {
            // Получаем удаленный счет
            Account deletedAccount = accountRepository.getAccountById(accountId).getValue();
            if (deletedAccount == null || !deletedAccount.isDeleted()) {
                return; // Счет не найден или уже активен
            }
            
            // Очищаем поля удаления
            deletedAccount.setDeleteTime(null);
            deletedAccount.setDeletedBy(null);
            deletedAccount.setUpdateTime(LocalDateTime.now());
            deletedAccount.setUpdatedBy(user);
            
            // Обновляем счет в базе
            accountRepository.updateAccount(deletedAccount, user);
        });
    }
    
    // Изменить позицию счета (сложная логика)
    public void changePosition(Account account, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = account.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все активные счета для переупорядочивания
            List<Account> allAccounts = accountRepository.getAllActiveAccounts().getValue();
            if (allAccounts == null) return;
            
            // Проверяем, что новая позиция валидна
            int maxPosition = allAccounts.size();
            if (newPosition < 1 || newPosition > maxPosition) {
                throw new IllegalArgumentException("Позиция вне диапазона: " + maxPosition);
            }
            
            // Переупорядочиваем позиции
            if (oldPosition < newPosition) {
                // Двигаем счет вниз: сдвигаем счета между старой и новой позицией вверх
                for (Account a : allAccounts) {
                    if (a.getId() != account.getId() && 
                        a.getPosition() > oldPosition && 
                        a.getPosition() <= newPosition) {
                        a.setPosition(a.getPosition() - 1);
                        accountRepository.updateAccount(a, user);
                    }
                }
            } else {
                // Двигаем счет вверх: сдвигаем счета между новой и старой позицией вниз
                for (Account a : allAccounts) {
                    if (a.getId() != account.getId() && 
                        a.getPosition() >= newPosition && 
                        a.getPosition() < oldPosition) {
                        a.setPosition(a.getPosition() + 1);
                        accountRepository.updateAccount(a, user);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущего счета
            account.setPosition(newPosition);
            accountRepository.updateAccount(account, user);
        });
    }
    
    // Получить или создать счет (как в вашем backend)
    public LiveData<Account> getOrCreateAccount(String title, int currencyId, int amount, int type) {
        MutableLiveData<Account> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            List<Account> accounts = accountRepository.getAllActiveAccounts().getValue();
            if (accounts != null) {
                for (Account account : accounts) {
                    if (account.getTitle().equals(title)) {
                        liveData.postValue(account);
                        return;
                    }
                }
            }
            
            // Если не найден - создаем новый
            Account newAccount = new Account();
            newAccount.setTitle(title);
            newAccount.setAmount(amount);
            newAccount.setType(type);
            newAccount.setCurrencyId(currencyId);
            newAccount.setClosed(0);
            newAccount.setPosition(1); // TODO: Получить следующую позицию
            
            accountRepository.insertAccount(newAccount, user);
            liveData.postValue(newAccount);
        });
        return liveData;
    }
    
    // Получить количество активных счетов
    public LiveData<Integer> getActiveAccountsCount() {
        return accountRepository.getActiveAccountsCount();
    }
    
    // Валидация счета
    public boolean validateAccount(Account account) {
        if (account.getTitle() == null || account.getTitle().trim().isEmpty()) {
            return false;
        }
        if (account.getCurrencyId() <= 0) {
            return false;
        }
        if (account.getType() < 1 || account.getType() > 3) {
            return false;
        }
        return true;
    }
} 