
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.constants.SqlConstants;
import com.sadengineer.budgetmaster.backend.entity.Account;

import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TABLE_ACCOUNTS;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ACTIVE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.DELETED_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ID_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TITLE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TYPE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.CURRENCY_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION_0_END;

import java.util.List;

/**
 * Data Access Object для работы с Account Entity
 */
@Dao
public interface AccountDao {

    /**
     * Общее количество счетов по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return общее количество счетов по фильтру
     */
    @Query("SELECT COUNT(*) FROM " + TABLE_ACCOUNTS + " WHERE " + ENTITY_FILTER_CONDITION)
    int count(EntityFilter filter);

    /**
     * Удаляет счет из базы данных
     * @param account счет для удаления
     */
    @Delete
    void delete(Account account);
    
    /**
     * Удаляет все счета из базы данных
     */
    @Query("DELETE FROM " + TABLE_ACCOUNTS)
    void deleteAll();
    
    /**
     * Получает все счета по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список счетов, отсортированных по позиции (счета с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + ENTITY_FILTER_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Account>> getAll(EntityFilter filter);

    /**
     * Получает все счета по ID валюты по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @param currencyId ID валюты
     * @return список счетов с указанным ID валюты, отсортированных по позиции (счета с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + CURRENCY_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Account>> getAllByCurrency(int currencyId, EntityFilter filter);

    /**
     * Получает все счета по типу по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @param type тип счета
     * @return список счетов с указанным типом, отсортированных по позиции (счета с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + TYPE_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Account>> getAllByType(int type, EntityFilter filter);
    
    /**
     * Получает счет по ID (включая удаленные)
     * @param id ID счета
     * @return счет с указанным ID
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + ID_CONDITION)
    LiveData<Account> getById(int id);
        
    /**
     * Получает счет по позиции (включая удаленные)
     * @param position позиция счета
     * @return счет с указанной позицией
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + POSITION_CONDITION)
    LiveData<Account> getByPosition(int position);
    
    /**
     * Получает счет по названию (включая удаленные)
     * @param title название счета
     * @return счет с указанным названием
     */
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + TITLE_CONDITION)
    LiveData<Account> getByTitle(String title);

    /**
     * Получает максимальную позицию среди счетов
     * @return максимальная позиция или 0, если счетов нет
     */
    @Query("SELECT COALESCE(MAX(position), 0) FROM " + TABLE_ACCOUNTS)
    int getMaxPosition();

    /**
     * Получает общую сумму баланса по ID валюты по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @param currencyId ID валюты
     * @return общая сумма баланса по ID валюты
     */
    @Query("SELECT SUM(amount) FROM " + TABLE_ACCOUNTS + " WHERE " + CURRENCY_CONDITION + " AND " + ENTITY_FILTER_CONDITION)
    Integer getTotalBalanceByCurrency(int currencyId, EntityFilter filter);
    
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
    @Query("SELECT * FROM " + TABLE_ACCOUNTS + " WHERE " + TITLE_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION)
    LiveData<List<Account>> searchByTitle(String searchQuery);
    
    /**
     * Сдвигает позиции счетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_ACCOUNTS + " SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции счетов вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_ACCOUNTS + " SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующий счет в базе данных
     * @param account счет для обновления
     */
    @Update
    void update(Account account);  
} 