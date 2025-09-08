package com.sadengineer.budgetmaster.backend.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

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
    @Query("SELECT COUNT(*) FROM budgets WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
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
    @Query("DELETE FROM budgets")
    void deleteAll();
    
    /**
     * Получает все бюджеты по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список бюджетов, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM budgets WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Budget>> getAll(EntityFilter filter);

    /**
     * Получает все бюджеты по типу операций категорий и по фильтру
     * @param operationType тип операции
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список активных бюджетов для расходов, отсортированных по позиции
     */
    @Query("SELECT b.* FROM budgets b " +
           "INNER JOIN categories c ON b.categoryId = c.id " +
           "WHERE b.deleteTime IS NULL AND c.deleteTime IS NULL AND " +
           "c.operationType = :operationType AND " +
           "((:filter = 'ACTIVE' AND b.deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND b.deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY b.position ASC")
    LiveData<List<Budget>> getAllByOperationType(int operationType, EntityFilter filter);

    /**
     * Получает все бюджеты по ID валюты и по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return список бюджетов с указанным ID валюты, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Budget>> getAllByCurrency(int currencyId, EntityFilter filter);
    
    /**
     * Получить все бюджеты по ID валюты (синхронно)
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return список бюджетов
     */
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    List<Budget> getAllByCurrencySync(int currencyId, EntityFilter filter);

    /**
     * Получает бюджет по ID категории (включая удаленные)
     * @param categoryId ID категории
     * @return бюджет с указанным ID категории
     */
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    LiveData<Budget> getByCategory(int categoryId);
    
    /**
     * Получить бюджет по категории синхронно (для транзакций)
     * @param categoryId ID категории
     * @return бюджет или null
     */
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    Budget getByCategorySync(int categoryId);
    
    /**
     * Получает бюджет по ID (включая удаленные)
     * @param id ID бюджета
     * @return бюджет с указанным ID
     */
    @Query("SELECT * FROM budgets WHERE id = :id")
    LiveData<Budget> getById(int id);
        
    /**
     * Получает бюджет по позиции (включая удаленные)
     * @param position позиция бюджета
     * @return бюджет с указанной позицией
     */
    @Query("SELECT * FROM budgets WHERE position = :position")
    LiveData<Budget> getByPosition(int position);

    /**
     * Получает максимальную позицию среди бюджетов
     * @return максимальная позиция или 0, если бюджетов нет
     */
    @Query("SELECT COALESCE(MAX(position), 0) FROM budgets")
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
    @Query("UPDATE budgets SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции бюджетов вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE budgets SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующий бюджет в базе данных
     * @param budget бюджет для обновления
     */
    @Update
    void update(Budget budget);  
    
    /**
     * Получает общую сумму бюджета по ID валюты по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @param currencyId ID валюты
     */
    @Query("SELECT SUM(amount) FROM budgets WHERE currencyId = :currencyId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
    LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter);

} 