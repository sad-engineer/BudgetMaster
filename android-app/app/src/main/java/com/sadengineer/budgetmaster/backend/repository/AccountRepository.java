package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.core.util.Pair;

import com.sadengineer.budgetmaster.backend.dao.AccountDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

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
     * Получить все счета по фильтру
     * @param filter фильтр для выборки счетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAll(EntityFilter filter) {
        return dao.getAll(filter);
    }

    /**
     * Получить все счета по ID валюты и по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр для выборки счетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByCurrency (int currencyId, EntityFilter filter) {
        return dao.getAllByCurrency(currencyId, filter);
    }

    /**
     * Получить все счета по типу и по фильтру
     * @param type тип счета
     * @param filter фильтр для выборки счетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех счетов
     */
    public LiveData<List<Account>> getAllByType(int type, EntityFilter filter) {
        return dao.getAllByType(type, filter);
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
     * Получить количество счетов по фильтру
     * @param filter фильтр для выборки счетов (ACTIVE, DELETED, ALL)
     * @return количество счетов
     */
    public int getCount(EntityFilter filter) {
        return dao.count(filter);
    }

    /**
     * Получить сумму на счетах по типу (текущие, сбережения, кредитные) с учетом фильтра и  Id валюты
     * @param currencyId ID валюты
     * @param type тип счета (1 - текущие, 2 - сбережения, 3 - кредитные)
     * @param filter фильтр для выборки счетов (ACTIVE, DELETED, ALL)
     * @return список пар "ID валюты - сумма" для указанной валюты
     */
    public LiveData<Long> getTotalAmountByCurrencyAndType(int currencyId, int type, EntityFilter filter) {
        return dao.getTotalAmountByCurrencyAndType(currencyId, type, filter);
    }
}
