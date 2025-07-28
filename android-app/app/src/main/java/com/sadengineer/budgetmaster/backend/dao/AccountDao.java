package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sadengineer.budgetmaster.backend.entity.Account;

import java.util.List;

/**
 * Data Access Object для работы с Account Entity
 */
@Dao
public interface AccountDao {
    
    @Query("SELECT * FROM accounts WHERE deleteTime IS NULL ORDER BY position ASC")
    List<Account> getAllActiveAccounts();
    
    @Query("SELECT * FROM accounts WHERE type = :type AND deleteTime IS NULL ORDER BY position ASC")
    List<Account> getAccountsByType(String type);
    
    @Query("SELECT * FROM accounts WHERE id = :id AND deleteTime IS NULL")
    Account getAccountById(int id);
    
    @Query("SELECT * FROM accounts WHERE currencyId = :currencyId AND deleteTime IS NULL ORDER BY position ASC")
    List<Account> getAccountsByCurrency(int currencyId);
    
    @Query("SELECT SUM(amount) FROM accounts WHERE currencyId = :currencyId AND deleteTime IS NULL")
    Integer getTotalBalanceByCurrency(int currencyId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAccount(Account account);
    
    @Update
    void updateAccount(Account account);
    
    @Delete
    void deleteAccount(Account account);
    
    @Query("UPDATE accounts SET deleteTime = :deleteTime, deletedBy = :deletedBy WHERE id = :id")
    void softDeleteAccount(int id, String deleteTime, String deletedBy);
    
    @Query("SELECT COUNT(*) FROM accounts WHERE deleteTime IS NULL")
    int getActiveAccountsCount();
    
    @Query("SELECT * FROM accounts WHERE deleteTime IS NOT NULL")
    List<Account> getAllDeletedAccounts();
    
    @Query("SELECT * FROM accounts WHERE title = :title AND deleteTime IS NULL")
    Account getAccountByTitle(String title);
    
    @Query("SELECT MAX(position) FROM accounts WHERE deleteTime IS NULL")
    Integer getMaxPosition();
    
    @Query("UPDATE accounts SET deleteTime = NULL, deletedBy = NULL, updateTime = :updateTime, updatedBy = :updatedBy WHERE id = :id")
    void restoreAccount(int id, String updateTime, String updatedBy);
    
    @Query("SELECT * FROM accounts WHERE position = :position AND deleteTime IS NULL")
    Account getAccountByPosition(int position);
} 