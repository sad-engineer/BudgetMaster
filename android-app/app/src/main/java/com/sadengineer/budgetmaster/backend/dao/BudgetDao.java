package com.sadengineer.budgetmaster.backend.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.constants.SqlConstants;
import com.sadengineer.budgetmaster.backend.entity.Budget;

import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TABLE_BUDGETS;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ENTITY_FILTER_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ACTIVE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.DELETED_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ID_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TITLE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TYPE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.PARENT_ID_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION_0_END; 

import java.util.List;

/**
 * Data Access Object для работы с Budget Entity
 */
@Dao
public interface BudgetDao {

    /**
     * Общее количество бюджетов по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return общее количество бюджетов по фильтру
     */
    @Query("SELECT COUNT(*) FROM " + TABLE_BUDGETS + " WHERE " + ENTITY_FILTER_CONDITION)
    int count(EntityFilter filter);

    /**
     * Удаляет бюджет из базы данных
     * @param budget бюджет для удаления
     */
    @Delete
    void delete(Budget budget);
    
    /**
     * Удаляет все бюджеты из базы данных
     */
    @Query("DELETE FROM " + TABLE_BUDGETS)
    void deleteAll();
    
    /**
     * Получает все бюджеты по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список бюджетов, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_BUDGETS + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Budget>> getAll(EntityFilter filter);

    /**
     * Получает все бюджеты по типу операций категорий и по фильтру
     * @param operationType тип операции
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список активных бюджетов для расходов, отсортированных по позиции
     */
    @Query("SELECT b.* FROM " + TABLE_BUDGETS + " b " +
           "INNER JOIN " + TABLE_CATEGORIES + " c ON b.categoryId = c.id " +
           "WHERE b.deleteTime IS NULL AND c.deleteTime IS NULL AND c.operationType = :operationType " +
           "ORDER BY b." + POSITION_SORT_CONDITION)
    LiveData<List<Budget>> getAllByOperationType(int operationType, EntityFilter filter);

    /**
     * Получает все бюджеты по ID валюты и по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список бюджетов с указанным ID валюты, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_BUDGETS + " WHERE " + CURRENCY_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Budget>> getAllByCurrency(int currencyId, EntityFilter filter);

    /**
     * Получает бюджет по ID категории (включая удаленные)
     * @param categoryId ID категории
     * @return бюджет с указанным ID категории
     */
    @Query("SELECT * FROM " + TABLE_BUDGETS + " WHERE " + CATEGORY_CONDITION)
    LiveData<Budget> getByCategory(int categoryId);
    
    /**
     * Получает бюджет по ID (включая удаленные)
     * @param id ID бюджета
     * @return бюджет с указанным ID
     */
    @Query("SELECT * FROM " + TABLE_BUDGETS + " WHERE " + ID_CONDITION)
    LiveData<Budget> getById(int id);
        
    /**
     * Получает бюджет по позиции (включая удаленные)
     * @param position позиция бюджета
     * @return бюджет с указанной позицией
     */
    @Query("SELECT * FROM " + TABLE_BUDGETS + " WHERE " + POSITION_CONDITION)
    LiveData<Budget> getByPosition(int position);

    /**
     * Получает максимальную позицию среди бюджетов
     * @return максимальная позиция или 0, если бюджетов нет
     */
    @Query("SELECT COALESCE(MAX(position), 0) FROM " + TABLE_BUDGETS)
    int getMaxPosition();

    /**
     * Вставляет новый бюджет в базу данных
     * @param budget бюджет для вставки
     * @return ID вставленного бюджета или -1 при конфликте по title
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Budget budget);
    
    /**
     * Сдвигает позиции бюджетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_BUDGETS + " SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции бюджетов вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_BUDGETS + " SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующий бюджет в базе данных
     * @param budget бюджет для обновления
     */
    @Update
    void update(Budget budget);  
} 