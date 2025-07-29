// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;

/**
 * Data Access Object для работы с Currency Entity
 */
@Dao
public interface CurrencyDao {
    
    @Query("SELECT * FROM currencies WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Currency> getAllActiveCurrencies();
    
    @Query("SELECT * FROM currencies WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Currency> getAllCurrencies();
    
    @Query("SELECT * FROM currencies WHERE id = :id AND deleteTime IS NULL")
    Currency getCurrencyById(int id);
    
    @Query("SELECT * FROM currencies WHERE title = :title AND deleteTime IS NULL")
    Currency getCurrencyByTitle(String title);
    
    @Query("SELECT * FROM currencies WHERE code = :code AND deleteTime IS NULL")
    Currency getCurrencyByCode(String code);
    
    @Query("SELECT * FROM currencies WHERE isDefault = 1 AND deleteTime IS NULL LIMIT 1")
    Currency getDefaultCurrency();
        
    @Query("SELECT * FROM currencies WHERE position = :position AND deleteTime IS NULL")
    Currency getCurrencyByPosition(int position);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCurrency(Currency currency);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Currency currency);
    
    @Update
    void updateCurrency(Currency currency);
    
    @Delete
    void deleteCurrency(Currency currency);
    
    @Query("UPDATE currencies SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteCurrency(int id, String deleteTime, String deletedBy);
    
    @Query("UPDATE currencies SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE title = :title")
    void softDeleteCurrencyByTitle(String title, String deleteTime, String deletedBy);
    
    @Query("SELECT COUNT(*) FROM currencies WHERE deleteTime IS NULL")
    int getActiveCurrenciesCount();
    
    @Query("SELECT * FROM currencies WHERE deleteTime IS NOT NULL")
    List<Currency> getAllDeletedCurrencies();
    
    @Query("SELECT MAX(position) FROM currencies WHERE deleteTime IS NULL")
    Integer getMaxPosition();
    
    @Query("UPDATE currencies SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreCurrency(int id, String updateTime, String updatedBy);
    
    @Query("DELETE FROM currencies")
    void deleteAll();
} 