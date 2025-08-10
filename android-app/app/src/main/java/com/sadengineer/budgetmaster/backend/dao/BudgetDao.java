package com.sadengineer.budgetmaster.backend.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Budget;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object для работы с Budget Entity
 */
@Dao
public interface BudgetDao {

      /**
     * Количество активных бюджетов
     * @return количество активных бюджетов
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE deleteTime IS NULL")
    int countActive();

    /**
     * Количество удаленных бюджетов
     * @return количество удаленных бюджетов
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE deleteTime IS NOT NULL")
    int countDeleted();

    /**
     * Общее количество бюджетов (включая удаленные)
     * @return общее количество бюджетов
     */
    @Query("SELECT COUNT(*) FROM budgets")
    int count();

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
     * Получает все бюджеты, включая удаленные
     * @return список бюджетов, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM budgets ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Budget>> getAll();

    /**
     * Получает все активные бюджеты
     * @return список активных счетов, отсортированных по позиции
     */
    @Query("SELECT * FROM budgets WHERE deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Budget>> getAllActive();

    /**
     * Получает все удаленные бюджеты
     * @return список удаленных бюджетов, отсортированных по позиции
     */
    @Query("SELECT * FROM budgets WHERE deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Budget>> getAllDeleted();

    /**
     * Получает все бюджеты по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return список бюджетов с указанным ID валюты, отсортированных по позиции (бюджеты с позицией 0 в конце)
     */
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Budget>> getAllByCurrency(int currencyId);
    
    /**
     * Получает все активные бюджеты по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return список бюджетов с указанным ID валюты
     */
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Budget>> getAllActiveByCurrency(int currencyId);

    /**
     * Получает все удаленные бюджеты по ID валюты
     * @param currencyId ID валюты
     * @return список удаленных бюджетов с указанным ID валюты
     */
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId AND deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Budget>> getAllDeletedByCurrency(int currencyId);

    /**
     * Получает бюджет по ID категории (включая удаленные)
     * @param categoryId ID категории
     * @return бюджет с указанным ID категории
     */
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    LiveData<Budget> getByCategory(int categoryId);
    
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
} 