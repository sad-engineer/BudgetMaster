
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.BudgetDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;
import com.sadengineer.budgetmaster.backend.entity.OperationTypeFilter;

import java.util.List;

/**
 * Repository класс для работы с Budget Entity
 */
public class BudgetRepository {
    
    private static final String TAG = "BudgetRepository";
    
    private final BudgetDao dao;
    
    public BudgetRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.budgetDao();
    }

    /**
     * Получить все бюджеты
     * @param filter фильтр для выборки бюджетов
     * @param operationType фильтр типа операции
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAll(EntityFilter filter, OperationTypeFilter operationType) {
        switch (filter) {
            case ACTIVE:
                switch (operationType) {
                    case EXPENSE:
                        return dao.getAllActiveByOperationType(OperationTypeFilter.EXPENSE.getIndex());
                    case INCOME:
                        return dao.getAllActiveByOperationType(OperationTypeFilter.INCOME.getIndex());
                    case ALL:
                    default:
                        return dao.getAllActive();
                }
            case DELETED:
                switch (operationType) {
                    case EXPENSE:
                        return dao.getAllDeletedByOperationType(OperationTypeFilter.EXPENSE.getIndex());
                    case INCOME:
                        return dao.getAllDeletedByOperationType(OperationTypeFilter.INCOME.getIndex());
                    case ALL:
                    default:
                        return dao.getAllDeleted();
                }
            case ALL:
            default:
                return dao.getAll();
        }
    }
    
    /**
     * Получить все бюджеты (включая удаленные)
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAll() {
        return dao.getAll();
    }   

    /**
     * Получить все бюджеты по ID валюты
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAllByCurrency(int currencyId, EntityFilter filter) {
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
     * Получить все бюджеты по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAllByCurrency(int currencyId) {
        return dao.getAllByCurrency(currencyId);
    }
    
    /**
     * Получить бюджет по ID категории (включая удаленные)
     * @param categoryId ID категории
     * @return LiveData с бюджетом
     */
    public LiveData<Budget> getByCategory(int categoryId) {
        return dao.getByCategory(categoryId);
    }
    
    /**
     * Получить бюджет по ID (включая удаленные)
     * @param id ID бюджета
     * @return LiveData с бюджетом
     */
    public LiveData<Budget> getById(int id) {
        return dao.getById(id);
    }   
    
    /**
     * Получить бюджет по позиции (включая удаленные)
     * @param position позиция бюджета
     * @return LiveData с бюджетом
     */
    public LiveData<Budget> getByPosition(int position) {
        return dao.getByPosition(position);
    }

    /**
     * Вставить новый бюджет
     * @param budget бюджет для вставки
     * @return LiveData с вставленным бюджетом
     */
    public LiveData<Budget> insert(Budget budget) {
        long id = dao.insert(budget);
        return dao.getById((int)id);
    }
    
    /**
     * Обновить бюджет
     * @param budget бюджет для обновления
     */
    public void update(Budget budget) {
        dao.update(budget);
    }
    
    /**
     * Удалить бюджет (полное удаление из БД)
     * @param budget бюджет для удаления
     */
    public void delete(Budget budget) {
        dao.delete(budget);
    }
    
    /**
     * Удалить все бюджеты
     */
    public void deleteAll() {
        dao.deleteAll();
    }
    
    /**
     * Получить максимальную позицию среди всех бюджетов
     * @return максимальная позиция
     */
    public int getMaxPosition() {
        return dao.getMaxPosition();
    }
    
    /**
     * Сдвинуть позиции бюджетов вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsUp(int fromPosition) {
        dao.shiftPositionsUp(fromPosition);
    }
    
    /**
     * Сдвинуть позиции бюджетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsDown(int fromPosition) {
        dao.shiftPositionsDown(fromPosition);
    }
    
    /**
     * Получить количество бюджетов
     * @param filter фильтр для выборки бюджетов
     * @return количество бюджетов
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
     * Получить общее количество бюджетов (включая удаленные)
     * @return общее количество бюджетов
     */
    public int getCount() {
        return dao.count();
    }
} 