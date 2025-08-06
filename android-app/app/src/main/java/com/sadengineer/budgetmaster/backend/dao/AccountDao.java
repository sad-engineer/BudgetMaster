
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;

import java.util.List;

/**
 * Data Access Object для работы с Account Entity
 */
@Dao
public interface AccountDao {
    /**
     * Количество активных счетов
     * @return количество активных счетов
     */
    @Query("SELECT COUNT(*) FROM accounts WHERE deleteTime IS NULL")
    int countActive();

    /**
     * Общее количество счетов (включая удаленные)
     * @return общее количество счетов
     */
    @Query("SELECT COUNT(*) FROM accounts")
    int count();

    /**
     * Удаляет счет из базы данных
     * @param account счет для удаления
     */
    @Delete
    void delete(Account account);
    
    /**
     * Удаляет все счета из базы данных
     */
    @Query("DELETE FROM accounts")
    void deleteAll();
    
    /**
     * Получает все счета, включая удаленные
     * @return список счетов, отсортированных по позиции
     */
    @Query("SELECT * FROM accounts ORDER BY position ASC")
    LiveData<List<Account>> getAll();

    /**
     * Получает все активные счета
     * @return список активных счетов, отсортированных по позиции
     */
    @Query("SELECT * FROM accounts WHERE deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Account>> getAllActive();

    /**
     * Получает все удаленные счета
     * @return список удаленных счетов, отсортированных по позиции
     */
    @Query("SELECT * FROM accounts WHERE deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Account>> getAllDeleted();

    /**
     * Получает все счета по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return список счетов с указанным ID валюты
     */
    @Query("SELECT * FROM accounts WHERE currencyId = :currencyId ORDER BY position ASC")
    LiveData<List<Account>> getAllByCurrency(int currencyId);
    
    /**
     * Получает все активные счета по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return список счетов с указанным ID валюты
     */
    @Query("SELECT * FROM accounts WHERE currencyId = :currencyId AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Account>> getAllActiveByCurrency(int currencyId);

    /**
     * Получает все счета по типу (включая удаленные)
     * @param type тип счета
     * @return список счетов с указанным типом
     */
    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY position ASC")
    LiveData<List<Account>> getAllByType(String type);

    /**
     * Получает все активные счета по типу (включая удаленные)
     * @param type тип счета
     * @return список счетов с указанным типом
     */
    @Query("SELECT * FROM accounts WHERE type = :type AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Account>> getAllActiveByType(String type);
    /**
     * Получает счет по ID (включая удаленные)
     * @param id ID счета
     * @return счет с указанным ID
     */
    @Query("SELECT * FROM accounts WHERE id = :id")
    LiveData<Account> getById(int id);
        
    /**
     * Получает счет по позиции (включая удаленные)
     * @param position позиция счета
     * @return счет с указанной позицией
     */
    @Query("SELECT * FROM accounts WHERE position = :position")
    LiveData<Account> getByPosition(int position);
    
    /**
     * Получает счет по названию (включая удаленные)
     * @param title название счета
     * @return счет с указанным названием
     */
    @Query("SELECT * FROM accounts WHERE title = :title")
    LiveData<Account> getByTitle(String title);

    /**
     * Получает максимальную позицию среди счетов
     * @return максимальная позиция или null, если счетов нет
     */
    @Query("SELECT MAX(position) FROM accounts")
    Integer getMaxPosition();

    /**
     * Получает общую сумму баланса по ID валюты (включая удаленные)
     * @param currencyId ID валюты
     * @return общая сумма баланса по ID валюты
     */
    @Query("SELECT SUM(amount) FROM accounts WHERE currencyId = :currencyId")
    Integer getTotalBalanceByCurrency(int currencyId);

    /**
     * Получает общую сумму баланса активных счетов по ID валюты
     * @param currencyId ID валюты
     * @return общая сумма баланса по ID валюты
     */
    @Query("SELECT SUM(amount) FROM accounts WHERE currencyId = :currencyId AND deleteTime IS NULL")
    Integer getTotalActiveBalanceByCurrency(int currencyId);

    /**
     * Вставляет новый счет в базу данных
     * @param account счет для вставки
     * @return ID вставленного счета или -1 при конфликте по title
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Account account);
    
    /**
     * Получает счета по подстроке в названии (включая удаленные)
     * @param searchQuery подстрока для поиска
     * @return список счетов, содержащих подстроку в названии
     */
    @Query("SELECT * FROM accounts WHERE title LIKE '%' || :searchQuery || '%' ORDER BY position ASC")
    LiveData<List<Account>> searchByTitle(String searchQuery);
    
    /**
     * Обновляет существующий счет в базе данных
     * @param account счет для обновления
     */
    @Update
    void update(Account account);  
} 