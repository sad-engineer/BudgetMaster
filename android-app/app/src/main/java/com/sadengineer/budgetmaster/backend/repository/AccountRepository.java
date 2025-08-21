
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;

import java.util.List;

/**
 * Repository класс для работы с Account Entity
 */
public class AccountRepository {

    private static final String TAG = "AccountRepository";
    
    private final AccountDao dao;
    
    public AccountRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.accountDao();
    }

    /**
     * Получить все счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll(EntityFilter filter) {
        switch (filter) {
            case ACTIVE:
                return dao.getAllActive();
            case DELETED:
                return dao.getAllDeleted();
            case ALL:
            default:
                return dao.getAll();
        }
    }
    
    /**
     * Получить все счета
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll() {
        return dao.getAll();
    }
    
    /**
     * Получить все счета по ID валюты
     * @param currencyId ID валюты
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByCurrency (int currencyId, EntityFilter filter) {
        switch (filter) {
            case ACTIVE:
                return dao.getAllActiveByCurrency(currencyId);
            case DELETED:
                return dao.getAllDeletedByCurrency(currencyId);
            case ALL:
            default:
                return dao.getAllByCurrency(currencyId);
        }
    }

    /**
     * Получить все счета по типу
     * @param type тип счета
     * @param filter фильтр для выборки счетов
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByType(EntityFilter filter, int type) {
        switch (filter) {
            case ACTIVE:
                return dao.getAllActiveByType(type);
            case DELETED:
                return dao.getAllDeletedByType(type);
            case ALL:
            default:
                return dao.getAllByType(type);
        }
    }
       
    /**
     * Получить счет по ID (включая удаленные)
     * @param id ID счета
     * @return LiveData с счетом
     */
    public LiveData<Account> getById(int id) {
        return dao.getById(id);
    }
    
    /**
     * Получить счет по названию (включая удаленные)
     * @param title название счета
     * @return LiveData с счетом
     */
    public LiveData<Account> getByTitle(String title) {
        return dao.getByTitle(title);
    }
    
    /**
     * Получить счет по позиции (включая удаленные)
     * @param position позиция счета
     * @return LiveData с счетом
     */
    public LiveData<Account> getByPosition(int position) {
        return dao.getByPosition(position);
    }

    /**
     * Вставить новый счет
     * @param account счет для вставки
     * @return LiveData с вставленным счетом
     */
    public LiveData<Account> insert(Account account) {
        long id = dao.insert(account);
        return dao.getById((int)id);
    }
    
    /**
     * Обновить счет
     * @param account счет для обновления
     */
    public void update(Account account) {
        dao.update(account);
    }
    
    /**
     * Удалить счет (полное удаление из БД)
     * @param account счет для удаления
     */
    public void delete(Account account) {
        dao.delete(account);
    }
    
    /**
     * Удалить все счета
     */
    public void deleteAll() {
        dao.deleteAll();
    }
    
    /**
     * Получить максимальную позицию среди всех счетов
     * @return максимальная позиция
     */
    public int getMaxPosition() {
        return dao.getMaxPosition();
    }
    
    /**
     * Сдвинуть позиции счетов вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsUp(int fromPosition) {
        dao.shiftPositionsUp(fromPosition);
    }
    
    /**
     * Сдвинуть позиции счетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsDown(int fromPosition) {
        dao.shiftPositionsDown(fromPosition);
    }
    
    /**
     * Получить количество счетов
     * @param filter фильтр для выборки счетов
     * @return количество счетов
     */
    public int getCount(EntityFilter filter) {
        switch (filter) {
            case ACTIVE:
                return dao.countActive();
            case DELETED:
                return dao.countDeleted();
            case ALL:
            default:
                return dao.count();
        }
    }

    /**
     * Получить общее количество счетов (включая удаленные)
     * @return общее количество счетов
     */
    public int getCount() {
        return dao.count();
    }
} 
