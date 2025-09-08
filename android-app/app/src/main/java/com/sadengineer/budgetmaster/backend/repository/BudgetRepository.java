package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.BudgetDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;

import java.util.List;

/**
 * Repository класс для работы с Budget Entity
 */
public class BudgetRepository {

    private final BudgetDao dao;
    
    public BudgetRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.budgetDao();
    }

    /**
     * Получить все бюджеты по фильтру
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAll(EntityFilter filter) {
        return dao.getAll(filter);
    }

    /**
     * Получить все бюджеты по типу операции и по фильтру
     * @param operationType фильтр типа операции (EXPENSE, INCOME, ALL)
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAllByOperationType(OperationTypeFilter operationType, EntityFilter filter) {
        int operationTypeIndex = operationType.getIndex();
        return dao.getAllByOperationType(operationTypeIndex, filter);
    }

    /**
     * Получить все бюджеты по ID валюты и по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAllByCurrency(int currencyId, EntityFilter filter) {
        return dao.getAllByCurrency(currencyId, filter);
    }   
    
    /**
     * Получить все бюджеты по ID валюты (синхронно)
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return список бюджетов
     */
    public List<Budget> getAllByCurrencySync(int currencyId, EntityFilter filter) {
        return dao.getAllByCurrencySync(currencyId, filter);
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
     * Получить бюджет по категории синхронно (для транзакций)
     * @param categoryId ID категории
     * @return бюджет или null
     */
    public Budget getByCategorySync(int categoryId) {
        return dao.getByCategorySync(categoryId);
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
     * Получить количество бюджетов по фильтру
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return количество бюджетов
     */
    public int getCount(EntityFilter filter) {
        return dao.count(filter);
    }

    /**
     * Получить общую сумму бюджета по ID валюты по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return общая сумма бюджета по ID валюты
     */
    public LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByCurrency(currencyId, filter);
    }

} 