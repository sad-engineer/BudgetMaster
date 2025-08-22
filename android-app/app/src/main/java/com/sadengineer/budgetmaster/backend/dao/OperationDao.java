
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.constants.SqlConstants;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object для работы с Operation Entity
 */
@Dao
public interface OperationDao {

    private final String ENTITY_FILTER_CONDITION = SqlConstants.ENTITY_FILTER_CONDITION;
    private final String TYPE_CONDITION = SqlConstants.TYPE_CONDITION;
    private final String ACCOUNT_CONDITION = SqlConstants.ACCOUNT_CONDITION;
    private final String CATEGORY_CONDITION = SqlConstants.CATEGORY_CONDITION;
    private final String CURRENCY_CONDITION = SqlConstants.CURRENCY_CONDITION;
    private final String ID_CONDITION = SqlConstants.ID_CONDITION;
    private final String DATE_CONDITION = SqlConstants.DATE_CONDITION;
    private final String YEAR_CONDITION = SqlConstants.YEAR_CONDITION;
    private final String MONTH_CONDITION = SqlConstants.MONTH_CONDITION;
    private final String DATE_RANGE_CONDITION = SqlConstants.DATE_RANGE_CONDITION;
    private final String YEAR_MONTH_CONDITION = SqlConstants.YEAR_MONTH_CONDITION;

    private final String TABLE = SqlConstants.TABLE_OPERATIONS;

    // ----- Работа с количеством операций -----

    /**
     * Подсчет операций с фильтром
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + ENTITY_FILTER_CONDITION)
    int count(EntityFilter filter);
    
    /**
     * Количество операций по типу с фильтром
     * @param type тип операции
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по типу
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
            "WHERE " + TYPE_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByType(String type, EntityFilter filter);
    
    /**
     * Количество операций по счету с фильтром
     * @param accountId ID счета
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по счету
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + ACCOUNT_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByAccount(int accountId, EntityFilter filter);

    /** 
     * Количество операций по категории с фильтром
     * @param categoryId ID категории
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по категории
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + CATEGORY_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByCategory(int categoryId, EntityFilter filter);
    
    /**
     * Количество операций по валюте с фильтром
     * @param currencyId ID валюты
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по валюте
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
            "WHERE " + CURRENCY_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByCurrency(int currencyId, EntityFilter filter);
    
    /**
     * Количество операций по дате с фильтром
     * @param date дата
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций по дате
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + DATE_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByDate(LocalDateTime date, EntityFilter filter);

    /**
     * Количество операций за месяц с фильтром
     * @param year год
     * @param month месяц
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за месяц
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + YEAR_MONTH_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByMonth(String year, String month, EntityFilter filter);

    /**
     * Количество операций за год с фильтром
     * @param year год
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за год
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + YEAR_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
    int countByYear(String year, EntityFilter filter);

    /**
     * Количество операций за период с фильтром
     * @param startDate начало периода
     * @param endDate конец периода
     * @param filter тип фильтра (ALL, ACTIVE, DELETED)
     * @return количество операций за период
     */
    @Query("SELECT COUNT(*) FROM " + TABLE +
           "WHERE " + DATE_RANGE_CONDITION + " AND " +
           ENTITY_FILTER_CONDITION)
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
    @Query("DELETE FROM " + TABLE)
    void deleteAll();

    // ----- Работа с получением операций -----

    /**
     * Получает все операции
     * @return все операции
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ENTITY_FILTER_CONDITION)
    List<Operation> getAll(EntityFilter filter);
    

    /**
     * Получает все операции по типу
     * @param type тип операции
     * @return все операции по типу
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + TYPE_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getAllByType(String type, EntityFilter filter);

    /** 
     * Получает все операции по счету
     * @param accountId ID счета
     * @return все операции по счету
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ACCOUNT_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getAllByAccount(int accountId, EntityFilter filter);

    /**
     * Получает все операции по категории
     * @param categoryId ID категории
     * @return все операции по категории
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + CATEGORY_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getAllByCategory(int categoryId, EntityFilter filter);

    /**
     * Получает все операции по валюте
     * @param currencyId ID валюты
     * @return все операции по валюте
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + CURRENCY_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getAllByCurrency(int currencyId, EntityFilter filter);

    /**
     * Получает счет по ID (включая удаленные)
     * @param id ID счета
     * @return счет с указанным ID
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ID_CONDITION)
    LiveData<Operation> getById(int id);
    
    /**
     * Получает операцию по дате
     * @param date дата
     * @return операцию по дате
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + DATE_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getByDate(LocalDateTime date, EntityFilter filter);

    /**
     * Получает операцию по месяцу
     * @param year год
     * @param month месяц
     * @return операцию по месяцу
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + YEAR_MONTH_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getByMonth(String year, String month, EntityFilter filter);

    /**
     * Получает операцию по году
     * @param year год
     * @return операцию по году
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + YEAR_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getByYear(String year, EntityFilter filter);

    /**
     * Получает операцию по периоду
     * @param startDate начало периода
     * @param endDate конец периода
     * @return операцию по периоду
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + DATE_RANGE_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    List<Operation> getByDateRange(LocalDateTime startDate, LocalDateTime endDate, EntityFilter filter);

    /**
     * Получает общую сумму баланса по типу (в зависимости от фильтра)
     * @param type тип операции
     * @return общая сумма баланса по типу
     */
    @Query("SELECT SUM(amount) FROM " + TABLE + " WHERE " + TYPE_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    Integer getTotalBalanceByType(int type, EntityFilter filter);

    /**
     * Получает общую сумму баланса по счету (в зависимости от фильтра)
     * @param accountId ID счета
     * @return общая сумма баланса по счету
     */
    @Query("SELECT SUM(amount) FROM " + TABLE + " WHERE " + ACCOUNT_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    Integer getTotalBalanceByAccount(int accountId, EntityFilter filter);

    /**
     * Получает общую сумму баланса по категории (в зависимости от фильтра)
     * @param categoryId ID категории
     * @return общая сумма баланса по категории
     */
    @Query("SELECT SUM(amount) FROM " + TABLE + " WHERE " + CATEGORY_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    Integer getTotalBalanceByCategory(int categoryId, EntityFilter filter);

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