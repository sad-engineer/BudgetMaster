
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Budget;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object для работы с Budget Entity
 */
@Dao
public interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getAllActiveBudgets();
    
    @Query("SELECT * FROM budgets WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getActiveBudgets();
    
    @Query("SELECT * FROM budgets WHERE id = :id AND deleteTime IS NULL")
    Budget getBudgetById(int id);
    
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getBudgetsByCategory(int categoryId);
    
    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getGeneralBudgets();
    
    @Query("SELECT * FROM budgets WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getBudgetsByDate();
    
    @Query("SELECT * FROM budgets WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getBudgetsByPeriod();
    
    @Query("SELECT * FROM budgets WHERE currencyId = :currencyId AND deleteTime IS NULL ORDER BY position ASC")
    List<Budget> getBudgetsByCurrency(int currencyId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBudget(Budget budget);
    
    @Update
    void updateBudget(Budget budget);
    
    @Delete
    void deleteBudget(Budget budget);
    
    @Query("UPDATE budgets SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteBudget(int id, String deleteTime, String deletedBy);
    

    
    @Query("SELECT COUNT(*) FROM budgets WHERE deleteTime IS NULL")
    int getActiveBudgetsCount();
    
    @Query("SELECT COUNT(*) FROM budgets WHERE deleteTime IS NULL")
    int getActiveBudgetsCountByStatus();
    
    @Query("SELECT * FROM budgets WHERE deleteTime IS NOT NULL")
    List<Budget> getAllDeletedBudgets();
    

    
    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND deleteTime IS NULL LIMIT 1")
    Budget getBudgetByCategoryId(int categoryId);
    
    @Query("SELECT MAX(position) FROM budgets WHERE deleteTime IS NULL")
    Integer getMaxPosition();
    
    @Query("UPDATE budgets SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreBudget(int id, String updateTime, String updatedBy);
    
    @Query("DELETE FROM budgets")
    void deleteAll();
    
    @Query("SELECT * FROM budgets WHERE position = :position AND deleteTime IS NULL")
    Budget getBudgetByPosition(int position);
    
    @Query("SELECT SUM(amount) FROM budgets WHERE currencyId = :currencyId AND deleteTime IS NULL")
    Integer getTotalAmountByCurrency(int currencyId);
} 