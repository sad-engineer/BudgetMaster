// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Operation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object для работы с Operation Entity
 */
@Dao
public interface OperationDao {
    
    @Query("SELECT * FROM operations WHERE deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getAllActiveOperations();
    
    @Query("SELECT * FROM operations WHERE type = :type AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByType(String type);
    
    @Query("SELECT * FROM operations WHERE accountId = :accountId AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByAccount(int accountId);
    
    @Query("SELECT * FROM operations WHERE categoryId = :categoryId AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByCategory(int categoryId);
    
    @Query("SELECT * FROM operations WHERE id = :id AND deleteTime IS NULL")
    Operation getOperationById(int id);
    
    @Query("SELECT * FROM operations WHERE operationDate BETWEEN :startDate AND :endDate AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(amount) FROM operations WHERE type = :type AND accountId = :accountId AND deleteTime IS NULL")
    Double getTotalAmountByTypeAndAccount(String type, int accountId);
    
    @Query("SELECT SUM(amount) FROM operations WHERE type = :type AND categoryId = :categoryId AND deleteTime IS NULL")
    Double getTotalAmountByTypeAndCategory(String type, int categoryId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOperation(Operation operation);
    
    @Update
    void updateOperation(Operation operation);
    
    @Delete
    void deleteOperation(Operation operation);
    
    @Query("UPDATE operations SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteOperation(int id, String deleteTime, String deletedBy);
    
    @Query("SELECT COUNT(*) FROM operations WHERE deleteTime IS NULL")
    int getActiveOperationsCount();
    
    @Query("SELECT COUNT(*) FROM operations WHERE type = :type AND deleteTime IS NULL")
    int getOperationsCountByType(String type);
    
    @Query("SELECT * FROM operations WHERE deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getAllOperations();
    
    @Query("SELECT * FROM operations WHERE currencyId = :currencyId AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByCurrency(int currencyId);
    
    @Query("SELECT * FROM operations WHERE DATE(operationDate) = DATE(:date) AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByDay(LocalDateTime date);
    
    @Query("SELECT * FROM operations WHERE description LIKE '%' || :comment || '%' AND deleteTime IS NULL ORDER BY operationDate DESC")
    List<Operation> getOperationsByComment(String comment);
    
    @Query("SELECT * FROM operations WHERE deleteTime IS NOT NULL")
    List<Operation> getAllDeletedOperations();
    
    @Query("UPDATE operations SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreOperation(int id, String updateTime, String updatedBy);
    
    @Query("DELETE FROM operations")
    void deleteAll();
    
    @Query("SELECT SUM(amount) FROM operations WHERE type = :type AND deleteTime IS NULL")
    Integer getTotalAmountByType(String type);
    
    @Query("SELECT SUM(amount) FROM operations WHERE accountId = :accountId AND deleteTime IS NULL")
    Integer getTotalAmountByAccount(int accountId);
    
    @Query("SELECT SUM(amount) FROM operations WHERE categoryId = :categoryId AND deleteTime IS NULL")
    Integer getTotalAmountByCategory(int categoryId);
    
    @Query("SELECT SUM(amount) FROM operations WHERE currencyId = :currencyId AND deleteTime IS NULL")
    Integer getTotalAmountByCurrency(int currencyId);
    
    @Query("SELECT COUNT(*) FROM operations WHERE deleteTime IS NULL")
    int getOperationsCount();
    
    @Query("SELECT SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END) FROM operations WHERE accountId = :accountId AND deleteTime IS NULL")
    Integer getAccountBalance(int accountId);
} 