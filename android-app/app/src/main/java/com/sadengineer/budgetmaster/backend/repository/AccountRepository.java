
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;

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
     * Получить все счета (включая удаленные)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll() {
        return dao.getAll();
    }
    
    /**
     * Получить все активные счета
     * @return LiveData со списком активных счетов
     */
    public LiveData<List<Account>> getAllActive() {
        return dao.getAllActive();
    }
    
    /**
     * Получить все удаленные счета
     * @return LiveData со списком удаленных счетов
     */
    public LiveData<List<Account>> getAllDeleted() {
        return dao.getAllDeleted();
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
        Integer maxPos = dao.getMaxPosition();
        return maxPos != null ? maxPos : 0;
    }
    
    /**
     * Получить количество активных счетов
     * @return количество активных счетов
     */
    public int getActiveCount() {
        return dao.countActive();
    }

    /**
     * Получить счета по подстроке в названии
     * @param searchQuery подстрока для поиска
     * @return LiveData с списком счетов
     */
    public LiveData<List<Account>> searchByTitle(String searchQuery) {
        return dao.searchByTitle(searchQuery);
    }
    
    /**
     * Получить общее количество счетов
     * @return общее количество счетов
     */
    public int getCount() {
        return dao.count();
    }
} 
