package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.OperationDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository класс для работы с Operation Entity
 */
public class OperationRepository {

    private final OperationDao dao;
    
    public OperationRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.operationDao();
    }

    /**
     * Подсчет операций с фильтром
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций
     */
    public int count(EntityFilter filter) {
        return dao.count(filter);
    }
    
    /**
     * Подсчет операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по типу
     */
    public int countByType(int type, EntityFilter filter) {
        return dao.countByType(type, filter);
    }
    
    /**
     * Подсчет операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по счету
     */
    public int countByAccount(int accountId, EntityFilter filter) {
        return dao.countByAccount(accountId, filter);
    }
    
    /**
     * Подсчет операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по категории
     */
    public int countByCategory(int categoryId, EntityFilter filter) {
        return dao.countByCategory(categoryId, filter);
    }
    
    /**
     * Подсчет операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по валюте
     */
    public int countByCurrency(int currencyId, EntityFilter filter) {
        return dao.countByCurrency(currencyId, filter);
    }
    
    /**
     * Подсчет операций за день с фильтром
     * @param date дата
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за день
     */
    public int countByDate(LocalDateTime date, EntityFilter filter) {
        return dao.countByDate(date, filter);
    }

    /**
     * Подсчет операций за месяц с фильтром
     * @param year год
     * @param month месяц
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за месяц 
     */
    public int countByMonth(String year, String month, EntityFilter filter) {
        return dao.countByMonth(year, month, filter);
    }
    
    /**
     * Подсчет операций за год с фильтром
     * @param year год
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за год
     */
    public int countByYear(String year, EntityFilter filter) {
        return dao.countByYear(year, filter);
    }

    /**
     * Подсчет операций за диапазон дат с фильтром
     * @param startDate начало диапазона
     * @param endDate конец диапазона
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за диапазон дат
     */
    public int countByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return dao.countByDateRange(startDate, endDate, filter);
    }

    /**
     * Удаляет операцию из базы данных
     * @param operation операция для удаления
     */
    public void delete(Operation operation) {
        dao.delete(operation);
    }

    /**
     * Удаляет все операции из базы данных
     */
    public void deleteAll() {
        dao.deleteAll();
    }

    /**
     * Получает все операции по фильтру
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAll(EntityFilter filter) {
        return dao.getAll(filter);
    }

    /**
     * Получает все операции по типу с фильтром
     * @param type тип операции
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByType(int type, EntityFilter filter) {
        return dao.getAllByType(type, filter);
    }

    /**
     * Получает все операции по счету с фильтром
     * @param accountId ID счета
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByAccount(int accountId, EntityFilter filter) {
        return dao.getAllByAccount(accountId, filter);
    }

    /**
     * Получает все операции по категории с фильтром
     * @param categoryId ID категории
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByCategory(int categoryId, EntityFilter filter) {
        return dao.getAllByCategory(categoryId, filter);
    }   

    /**
     * Получает все операции по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByCurrency(int currencyId, EntityFilter filter) {
        return dao.getAllByCurrency(currencyId, filter);
    }
    
    /**
     * Получить все операции по ID валюты (синхронно)
     * @param currencyId ID валюты
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public List<Operation> getAllByCurrencySync(int currencyId, EntityFilter filter) {
        return dao.getAllByCurrencySync(currencyId, filter);
    }

    /**
     * Получает все операции по дате с фильтром
     * @param date дата
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByDate(LocalDateTime date, EntityFilter filter) {
        return dao.getAllByDate(date, filter);
    }

    /**
     * Получает все операции по месяцу с фильтром
     * @param year год
     * @param month месяц
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByMonth(String year, String month, EntityFilter filter) {
        return dao.getAllByMonth(year, month, filter);
    }   

    /**
     * Получает все операции по году с фильтром
     * @param year год
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByYear(String year, EntityFilter filter) {
        return dao.getAllByYear(year, filter);
    }
    
    /**
     * Получает все операции по периоду с фильтром
     * @param startDate начало периода
     * @param endDate конец периода
     * @param filter фильтр для выборки операций (ACTIVE, DELETED, ALL)
     * @return список операций
     */
    public LiveData<List<Operation>> getAllByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return dao.getAllByDateRange(startDate, endDate, filter);   
    }
    
    /**
     * Получает операцию по ID
     * @param id ID операции
     * @return операция
     */
    public LiveData<Operation> getById(int id) {
        return dao.getById(id);
    }
    
    /**
     * Получает общую сумму операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по типу
     */
    public LiveData<Long> getTotalAmountByType(int type, EntityFilter filter) {
        return dao.getTotalAmountByType(type, filter);
    }

    /**
     * Получает общую сумму операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по счету
     */
    public LiveData<Long> getTotalAmountByAccount(int accountId, EntityFilter filter) {
        return dao.getTotalAmountByAccount(accountId, filter);
    }

    /**
     * Получает общую сумму операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по категории
     */ 
    public LiveData<Long> getTotalAmountByCategory(int categoryId, EntityFilter filter) {
        return dao.getTotalAmountByCategory(categoryId, filter);
    }

    /**
     * Получает общую сумму операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по валюте
     */
    public LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByCurrency(currencyId, filter);
    }

    /**
     * Получает общую сумму операций по валюте за период с фильтром
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return общая сумма операций по валюте за период
     */
    public LiveData<Long> getTotalAmountByCurrencyByDateRange(LocalDateTime startDate, LocalDateTime endDate, int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByCurrencyByDateRange(startDate, endDate, currencyId, filter);
    }

    /**
     * Получает сумму расходов за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return сумма расходов за период
     */
    public LiveData<Long> getExpenseSumByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return dao.getExpenseSumByDateRange(startDate, endDate, filter);
    }

    /**
     * Получает сумму доходов за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return сумма доходов за период
     */
    public LiveData<Long> getIncomeSumByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return dao.getIncomeSumByDateRange(startDate, endDate, filter);
    }

    /**
     * Получает общую сумму операций за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param filter фильтр сущностей
     * @return общая сумма операций за период
     */
    public LiveData<Long> getTotalAmountByDateRange(LocalDateTime startDate, LocalDateTime endDate, int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByDateRange(startDate, endDate, currencyId, filter);
    }

    /**
     * Получает общую сумму операций по типу за период
     * @param type тип операций
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param filter фильтр сущностей
     * @return общая сумма операций по типу за период
     */
    public LiveData<Long> getTotalAmountByTypeAndDateRange(int type, LocalDateTime startDate, LocalDateTime endDate, int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByTypeAndDateRange(type, startDate, endDate, currencyId, filter);
    }

    /**
     * Получить операции по типу и диапазону дат
     * @param type тип операции
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @param filter фильтр для выборки операций
     * @return LiveData со списком операций
     */
    public LiveData<List<Operation>> getByTypeAndDateRange(int type, LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter) {
        return dao.getByTypeAndDateRange(type, startDate, endDate, filter);
    }

    /**
     * Получает общую сумму операций по категории за период
     * @param categoryId ID категории
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @param filter фильтр сущностей
     * @return общая сумма операций по категории за период
     */
    public LiveData<Long> getTotalAmountByCategoryAndDateRange(Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, int currencyId, EntityFilter filter) {
        return dao.getTotalAmountByCategoryAndDateRange(categoryId, startDate, endDate, currencyId, filter);
    }

    /**
     * Получает общую сумму операций за месяц
     * @param year год
     * @param month месяц
     * @param filter фильтр сущностей
     * @return общая сумма операций за месяц
     */
    public LiveData<Long> getTotalAmountByMonth(String year, String month, EntityFilter filter) {
        return dao.getTotalAmountByMonth(year, month, filter);
    }

    /**
     * Получает общую сумму операций по типу за месяц
     * @param type тип операций
     * @param year год
     * @param month месяц
     * @param filter фильтр сущностей
     * @return общая сумма операций по типу за месяц
     */
    public LiveData<Long> getTotalAmountByTypeAndMonth(int type, String year, String month, EntityFilter filter) {
        return dao.getTotalAmountByTypeAndMonth(type, year, month, filter);
    }

    /**
     * Получает общую сумму операций за год
     * @param year год
     * @param filter фильтр сущностей
     * @return общая сумма операций за год
     */
    public LiveData<Long> getTotalAmountByYear(String year, EntityFilter filter) {
        return dao.getTotalAmountByYear(year, filter);
    }

    /**
     * Получает общую сумму операций по типу за год
     * @param type тип операций
     * @param year год
     * @param filter фильтр сущностей
     * @return общая сумма операций по типу за год
     */
    public LiveData<Long> getTotalAmountByTypeAndYear(int type, String year, EntityFilter filter) {
        return dao.getTotalAmountByTypeAndYear(type, year, filter);
    }

    /**
     * Вставить новую операцию
     * @param operation операция для вставки
     * @return операция
     */
    public LiveData<Operation> insert(Operation operation) {
        long id = dao.insert(operation);
        return dao.getById((int)id);
    }

    /**
     * Обновить операцию
     * @param operation операция для обновления
     */
    public void update(Operation operation) {
        dao.update(operation);
    }

} 