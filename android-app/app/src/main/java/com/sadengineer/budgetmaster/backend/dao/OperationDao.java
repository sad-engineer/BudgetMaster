package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object для работы с Operation Entity
 */
@Dao
public interface OperationDao {

    // ----- Работа с количеством операций -----

    /**
     * Подсчет операций с фильтром
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций
     */
    @Query("SELECT COUNT(*) FROM operations WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int count(EntityFilter filter);
    
    /**
     * Количество операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по типу
     */
    @Query("SELECT COUNT(*) FROM operations WHERE type = :type AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByType(int type, EntityFilter filter);
    
    /**
     * Количество операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по счету
     */
    @Query("SELECT COUNT(*) FROM operations WHERE accountId = :accountId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByAccount(int accountId, EntityFilter filter);

    /** 
     * Количество операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по категории
     */
    @Query("SELECT COUNT(*) FROM operations WHERE categoryId = :categoryId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByCategory(int categoryId, EntityFilter filter);
    
    /**
     * Количество операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по валюте
     */
    @Query("SELECT COUNT(*) FROM operations WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByCurrency(int currencyId, EntityFilter filter);
    
    /**
     * Количество операций по дате с фильтром
     * @param date дата
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по дате
     */
    @Query("SELECT COUNT(*) FROM operations WHERE operationDate = :date AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByDate(LocalDateTime date, EntityFilter filter);

    /**
     * Количество операций за месяц с фильтром
     * @param year год
     * @param month месяц
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за месяц
     */
    @Query("SELECT COUNT(*) FROM operations WHERE strftime('%Y', operationDate) = :year AND strftime('%m', operationDate) = :month AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByMonth(String year, String month, EntityFilter filter);

    /**
     * Количество операций за год с фильтром
     * @param year год
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за год
     */
    @Query("SELECT COUNT(*) FROM operations WHERE strftime('%Y', operationDate) = :year AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByYear(String year, EntityFilter filter);

    /**
     * Количество операций за период с фильтром
     * @param startDate начало периода
     * @param endDate конец периода
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за период
     */
    @Query("SELECT COUNT(*) FROM operations WHERE operationDate BETWEEN :startDate AND :endDate AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    int countByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter);

    // ----- Работа с удалением операций -----

    /**
     * Удаляет операцию из базы данных
     * @param operation операция для удаления
     */
    @Delete
    void delete(Operation operation);
    
    /**
     * Удаляет все операции из базы данных
     */
    @Query("DELETE FROM operations")
    void deleteAll();

    // ----- Работа с получением операций -----

    /**
     * Получает все операции
     * @return все операции
     */
    @Query("SELECT * FROM operations WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAll(EntityFilter filter);
    

    /**
     * Получает все операции по типу
     * @param type тип операции
     * @return все операции по типу
     */
    @Query("SELECT * FROM operations WHERE type = :type AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByType(int type, EntityFilter filter);

    /** 
     * Получает все операции по счету
     * @param accountId ID счета
     * @return все операции по счету
     */
    @Query("SELECT * FROM operations WHERE accountId = :accountId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByAccount(int accountId, EntityFilter filter);

    /**
     * Получает все операции по категории
     * @param categoryId ID категории
     * @return все операции по категории
     */
    @Query("SELECT * FROM operations WHERE categoryId = :categoryId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByCategory(int categoryId, EntityFilter filter);

    /**
     * Получает все операции по валюте
     * @param currencyId ID валюты
     * @return все операции по валюте
     */
    @Query("SELECT * FROM operations WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByCurrency(int currencyId, EntityFilter filter);
       
    /**
     * Получает все операции по дате
     * @param date дата
     * @return все операции по дате
     */
    @Query("SELECT * FROM operations WHERE operationDate = :date AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByDate(LocalDateTime date, EntityFilter filter);

    /**
     * Получает все операции по месяцу
     * @param year год
     * @param month месяц
     * @return все операции по месяцу
     */
    @Query("SELECT * FROM operations WHERE strftime('%Y', operationDate) = :year AND strftime('%m', operationDate) = :month AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByMonth(String year, String month, EntityFilter filter);

    /**
     * Получает все операции по году
     * @param year год
     * @return все операции по году
     */
    @Query("SELECT * FROM operations WHERE strftime('%Y', operationDate) = :year AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByYear(String year, EntityFilter filter);

    /**
     * Получает все операции по периоду
     * @param startDate начало периода
     * @param endDate конец периода
     * @return все операции по периоду
     */
    @Query("SELECT * FROM operations WHERE operationDate BETWEEN :startDate AND :endDate AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<List<Operation>> getAllByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter);

    /**
     * Получает счет по ID (включая удаленные)
     * @param id ID счета
     * @return счет с указанным ID
     */
    @Query("SELECT * FROM operations WHERE id = :id")
    LiveData<Operation> getById(int id);

    /**
     * Получает общую сумму баланса по типу (в зависимости от фильтра)
     * @param type тип операции
     * @return общая сумма баланса по типу
     */
    @Query("SELECT SUM(amount) FROM operations WHERE type = :type AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByType(int type, EntityFilter filter);

    /**
     * Получает общую сумму баланса по счету (в зависимости от фильтра)
     * @param accountId ID счета
     * @return общая сумма баланса по счету
     */
    @Query("SELECT SUM(amount) FROM operations WHERE accountId = :accountId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByAccount(int accountId, EntityFilter filter);

    /**
     * Получает общую сумму баланса по категории (в зависимости от фильтра)
     * @param categoryId ID категории
     * @return общая сумма баланса по категории
     */
    @Query("SELECT SUM(amount) FROM operations WHERE categoryId = :categoryId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByCategory(int categoryId, EntityFilter filter);
    
    /**
     * Получает общую сумму баланса по валюте (в зависимости от фильтра)
     * @param currencyId ID валюты
     * @return общая сумма баланса по валюте
     */
    @Query("SELECT SUM(amount) FROM operations WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter);

    /**
     * Получает общую сумму баланса по валюте за период (в зависимости от фильтра)
     * @param startDate начало периода
     * @param endDate конец периода
     * @param currencyId ID валюты
     * @return общая сумма баланса по валюте
     */
    @Query("SELECT SUM(amount) FROM operations WHERE currencyId = :currencyId AND operationDate BETWEEN :startDate AND :endDate AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByCurrencyByDateRange(LocalDateTime startDate, LocalDateTime endDate, int currencyId, EntityFilter filter);
       
    /**
     * Получает сумму расходов за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return сумма расходов за период
     */
    @Query("SELECT SUM(amount) FROM operations WHERE operationDate BETWEEN :startDate AND :endDate AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getExpenseSumByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получает сумму доходов за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return сумма доходов за период
     */
    @Query("SELECT SUM(amount) FROM operations WHERE operationDate BETWEEN :startDate AND :endDate AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getIncomeSumByDateRange(LocalDateTime startDate, LocalDateTime endDate);


    //TODO: Прописать здесь специальные методы для получения операций какому либо условию

    // ----- Работа с вставкой операций -----

    /**
     * Вставляет новую операцию в базу данных
     * @param operation операция для вставки
     * @return ID вставленной операции
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Operation operation);

    // ----- Работа с обновлением операций -----

    /**
     * Обновляет существующую операцию в базе данных
     * @param operation операция для обновления
     */
    @Update
    void update(Operation operation);
} 