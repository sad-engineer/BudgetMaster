
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository класс для работы с Account Entity
 */
public class AccountRepository {
    
    private final AccountDao accountDao;
    private final ExecutorService executorService;
    
    public AccountRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.accountDao = database.accountDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    // Получить все активные счета
    public LiveData<List<Account>> getAllActiveAccounts() {
        MutableLiveData<List<Account>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Account> accounts = accountDao.getAllActiveAccounts();
            liveData.postValue(accounts);
        });
        return liveData;
    }
    
    // Получить счета по типу
    public LiveData<List<Account>> getAccountsByType(String type) {
        MutableLiveData<List<Account>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Account> accounts = accountDao.getAccountsByType(type);
            liveData.postValue(accounts);
        });
        return liveData;
    }
    
    // Получить счет по ID
    public LiveData<Account> getAccountById(int id) {
        MutableLiveData<Account> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Account account = accountDao.getAccountById(id);
            liveData.postValue(account);
        });
        return liveData;
    }
    
    // Получить счета по валюте
    public LiveData<List<Account>> getAccountsByCurrency(int currencyId) {
        MutableLiveData<List<Account>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Account> accounts = accountDao.getAccountsByCurrency(currencyId);
            liveData.postValue(accounts);
        });
        return liveData;
    }
    
    // Получить общий баланс по валюте
    public LiveData<Integer> getTotalBalanceByCurrency(int currencyId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = accountDao.getTotalBalanceByCurrency(currencyId);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Вставить новый счет
    public void insertAccount(Account account, String createdBy) {
        executorService.execute(() -> {
            account.setCreateTime(LocalDateTime.now());
            account.setCreatedBy(createdBy);
            account.setUpdateTime(LocalDateTime.now());
            account.setUpdatedBy(createdBy);
            accountDao.insertAccount(account);
        });
    }
    
    // Обновить счет
    public void updateAccount(Account account, String updatedBy) {
        executorService.execute(() -> {
            account.setUpdateTime(LocalDateTime.now());
            account.setUpdatedBy(updatedBy);
            accountDao.updateAccount(account);
        });
    }
    
    // Удалить счет (soft delete)
    public void deleteAccount(int accountId, String deletedBy) {
        executorService.execute(() -> {
            accountDao.softDeleteAccount(accountId, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Получить количество активных счетов
    public LiveData<Integer> getActiveAccountsCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = accountDao.getActiveAccountsCount();
            liveData.postValue(count);
        });
        return liveData;
    }
    
    // Получить все удаленные счета
    public LiveData<List<Account>> getAllDeletedAccounts() {
        MutableLiveData<List<Account>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Account> accounts = accountDao.getAllDeletedAccounts();
            liveData.postValue(accounts);
        });
        return liveData;
    }
    
    // Получить счет по названию
    public LiveData<Account> getAccountByTitle(String title) {
        MutableLiveData<Account> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Account account = accountDao.getAccountByTitle(title);
            liveData.postValue(account);
        });
        return liveData;
    }
    
    // Получить максимальную позицию
    public LiveData<Integer> getMaxPosition() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer maxPos = accountDao.getMaxPosition();
            liveData.postValue(maxPos != null ? maxPos : 0);
        });
        return liveData;
    }
    
    // Восстановить счет
    public void restoreAccount(int accountId, String updatedBy) {
        executorService.execute(() -> {
            accountDao.restoreAccount(accountId, LocalDateTime.now().toString(), updatedBy);
        });
    }
    
    // Получить счет по позиции
    public LiveData<Account> getAccountByPosition(int position) {
        MutableLiveData<Account> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Account account = accountDao.getAccountByPosition(position);
            liveData.postValue(account);
        });
        return liveData;
    }
} 