package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.AccountRepository;

import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Account
 */
public class AccountService {
    private static final String TAG = "AccountService";
    
    private final AccountRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final ServiceConstants constants;
    private final AccountValidator validator;

    public AccountService(Context context, String user) {
        this.repo = new AccountRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.constants = new ServiceConstants();
        this.validator = new AccountValidator();
    }

    /**
     * Изменить позицию счета
     * @param account счет
     * @param newPosition новая позиция
     */
    public void changePosition(Account account, int newPosition) {
        executorService.execute(() -> {
            changePositionInTransaction(account, newPosition);
        });
    }
    
    /**
     * Транзакция для изменения позиции счета
     * @param account счет
     * @param newPosition новая позиция
     */
    @Transaction
    private void changePositionInTransaction(Account account, int newPosition) {
        int oldPosition = account.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return;
        }
        
        // Используем методы сдвига позиций из Repository
        if (oldPosition < newPosition) {
            repo.shiftPositionsDown(oldPosition);
            repo.shiftPositionsUp(newPosition + 1);
        } else {
            repo.shiftPositionsUp(newPosition);
            repo.shiftPositionsDown(oldPosition);
        }
        
        // Устанавливаем новую позицию для текущего счета
        account.setPosition(newPosition);
        repo.update(account);
    }
    
    /**
     * Изменить позицию счета по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        Account account = repo.getByPosition(oldPosition).getValue();
        if (account != null) {
            changePosition(account, newPosition);
        }
    }
    
    /**
     * Изменить позицию счета по названию
     * @param title название счета
     * @param newPosition новая позиция
     */
    public void changePosition(String title, int newPosition) {
        Account account = repo.getByTitle(title).getValue();
        if (account != null) {
            changePosition(account, newPosition);
        }
    }

    /**
     * Создать новый счет
     * @param title название счета
     * @param currencyId ID валюты
     * @param amount сумма
     * @param type тип счета
     * @param closed признак закрытости счета (0 - открыт, 1 - закрыт)
     */
    public void create(String title, Integer currencyId, Long amount, Integer type, Integer closed) {
        validator.validateTitle(title);
        validator.validateAmount(amount);
        validator.validateType(type);
        validator.validateClosed(closed);

        executorService.execute(() -> {
            validator.validateCurrencyId(currencyId, repo.getCount(EntityFilter.ALL));
            createAccountInTransaction(title, currencyId, amount, type, closed);
        });
    }   

    /**
     * Создать новый счет со значениями по умолчанию
     * @param title название счета
     */
    public void create(String title) {
        validator.validateTitle(title);
        executorService.execute(() -> {
            create(title, null, null, null, null);                
        });
    }

    /** Создать новый счет с проверенными значениями 
     * @param title название счета  
     * @param currencyId ID валюты
     * @param amount сумма
     * @param type тип счета
     * @param closed признак закрытости счета (0 - открыт, 1 - закрыт)
     */
    public void createWithoutValidation(String title, int currencyId, long amount, int type, int closed) {
        executorService.execute(() -> {
            createAccountInTransaction(title, currencyId, amount, type, closed);
        });
    }

    /**
     * Транзакция для создания нового счета
     * @param title название счета
     * @param amount сумма
     * @param currencyId ID валюты
     * @param type тип счета
     * @param closed признак закрытости счета (0 - открыт, 1 - закрыт)
     */
    @Transaction
    private void createAccountInTransaction(String title, int currencyId, long amount, int type, int closed) {
        Log.d(TAG, String.format(constants.MSG_CREATE_ACCOUNT_REQUEST, title));
        Account account = new Account();
        account.setTitle(title);
        account.setAmount(amount);
        account.setCurrencyId(currencyId);
        account.setType(type);
        account.setClosed(closed);
        account.setPosition(repo.getMaxPosition() + 1);
        account.setCreateTime(LocalDateTime.now());
        account.setCreatedBy(user);
        try {
            repo.insert(account);
            Log.d(TAG, String.format(constants.MSG_ACCOUNT_CREATED, title));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_CREATE_ACCOUNT_ERROR, title) + e.getMessage(), e);
        }
    }

    /**
     * Удалить счет (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param account счет
     */
    public void delete(Account account, boolean softDelete) {
        if (account == null) {
            Log.e(TAG, constants.MSG_DELETE_ACCOUNT_NOT_FOUND);
            return;
        }
        if (softDelete) {
            softDelete(account);
        } else {
            delete(account);
        }
    }

    /**
     * Удалить счет (полное удаление - удаление строки из БД)
     * @param account счет
     */
    private void delete(Account account) {
        executorService.execute(() -> {
            deleteAccountInTransaction(account);
        });
    }     
    
    /**
     * Транзакция для удаления валюты
     * @param account счет
     */
    @Transaction
    private void deleteAccountInTransaction(Account account) {
        Log.d(TAG, String.format(constants.MSG_DELETE_ACCOUNT_REQUEST, account.getTitle()));
        try {
            repo.delete(account);
            Log.d(TAG, String.format(constants.MSG_ACCOUNT_DELETED, account.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_DELETE_ACCOUNT_ERROR, account.getTitle()) + e.getMessage(), e);
        }
    }   
    
    /**
     * Получить все счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    }

    /**
     * Получить все счета по типу
     * @param type тип счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByType(int type, EntityFilter filter ) {
        return repo.getAllByType(type, filter);
    }
    
    /**
     * Получить все счета (включая удаленные)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll() {
        return repo.getAll(EntityFilter.ALL);
    }

    /**
     * Получить счет по названию
     * @param title название счета
     * @return LiveData с счетом
     */
    public LiveData<Account> getByTitle(String title) {
        return repo.getByTitle(title);
    }

    /**
     * Восстановить удаленный счет (soft delete)
     * @param deletedAccount удаленный счет
     */
    public void restore(Account deletedAccount) {
        executorService.execute(() -> {
            restoreAccountInTransaction(deletedAccount);
        });
    }
    
    /**
     * Транзакция для восстановления счета
     * @param deletedAccount удаленный счет
     */
    @Transaction
    private void restoreAccountInTransaction(Account deletedAccount) {
        Log.d(TAG, String.format(constants.MSG_RESTORE_ACCOUNT_REQUEST, deletedAccount.getTitle()));
        deletedAccount.setPosition(repo.getMaxPosition() + 1);
        deletedAccount.setDeleteTime(null);
        deletedAccount.setDeletedBy(null);
        deletedAccount.setUpdateTime(LocalDateTime.now());
        deletedAccount.setUpdatedBy(user);
        try {
            repo.update(deletedAccount);
            Log.d(TAG, String.format(constants.MSG_ACCOUNT_RESTORED, deletedAccount.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_RESTORE_ACCOUNT_ERROR, deletedAccount.getTitle()) + e.getMessage(), e);
        }
    }

    /**
     * Удалить счет (soft delete)
     * @param account счет
     */
    private void softDelete(Account account) {
        executorService.execute(() -> {
            softDeleteAccountInTransaction(account);
        });
    }

    /**
     * Транзакция для удаления счета (soft delete)
     * @param account счет
     */
    @Transaction
    private void softDeleteAccountInTransaction(Account account) {
        Log.d(TAG, String.format(constants.MSG_SOFT_DELETE_ACCOUNT_REQUEST, account.getTitle()));
        int deletedPosition = account.getPosition();
        account.setPosition(0);
        account.setDeleteTime(LocalDateTime.now());
        account.setDeletedBy(user);
        try {
            repo.update(account);
            // Пересчитываем позиции после soft delete
            repo.shiftPositionsDown(deletedPosition);
            Log.d(TAG, String.format(constants.MSG_ACCOUNT_SOFT_DELETED, account.getTitle()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_SOFT_DELETE_ACCOUNT_ERROR, account.getTitle()) + e.getMessage(), e);
        }
    }

    /**
     * Обновить счет
     * @param account счет
     */
    public void update(Account account) {
        if (account == null) {
            Log.e(TAG, constants.MSG_UPDATE_ACCOUNT_NOT_FOUND);
            return;
        }

        executorService.execute(() -> {
            Log.d(TAG, String.format(constants.MSG_UPDATE_ACCOUNT_REQUEST, account.getTitle()));
            account.setUpdateTime(LocalDateTime.now());
            account.setUpdatedBy(user);
            try {
                repo.update(account);
                Log.d(TAG, String.format(constants.MSG_ACCOUNT_UPDATED, account.getTitle()));
            } catch (Exception e) {
                Log.e(TAG, String.format(constants.MSG_UPDATE_ACCOUNT_ERROR, account.getTitle()) + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Получить количество счетов
     * @param filter фильтр для выборки счетов
     * @return количество счетов
     */
    public int getCount(EntityFilter filter) {
        return repo.getCount(filter);
    }
    
    /**
     * Получить общее количество счетов (включая удаленные)
     * @return общее количество счетов
     */
    public int getCount() {
        return repo.getCount(EntityFilter.ALL);
    }

    /**
     * Получить сводку по валютам для определенного типа счетов
     * @param type тип счета (1 - текущие, 2 - сбережения, 3 - кредитные)
     * @param filter фильтр для выборки счетов
     * @return список пар (currencyId, amount)
     */
    public List<KeyValuePair> getCurrencySummaryByType(int type, EntityFilter filter) {
        return repo.getCurrencySummaryByType(type, filter);
    }



} 