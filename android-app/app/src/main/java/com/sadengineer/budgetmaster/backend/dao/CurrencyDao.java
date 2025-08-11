
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;

/**
 * Data Access Object для работы с Currency Entity
 */
@Dao
public interface CurrencyDao {
    /**
     * Количество активных валют
     * @return количество активных валют
     */
    @Query("SELECT COUNT(*) FROM currencies WHERE deleteTime IS NULL")
    int countActive();

    /**
     * Количество удаленных валют
     * @return количество удаленных валют
     */
    @Query("SELECT COUNT(*) FROM currencies WHERE deleteTime IS NOT NULL")
    int countDeleted();

    /**
     * Общее количество валют (включая удаленные)
     * @return общее количество валют
     */
    @Query("SELECT COUNT(*) FROM currencies")
    int count();

    /**
     * Удаляет валюту из базы данных
     * @param currency валюта для удаления
     */
    @Delete
    void delete(Currency currency);
    
    /**
     * Удаляет все валюты из базы данных
     */
    @Query("DELETE FROM currencies")
    void deleteAll();
    
    /**
     * Получает все валюты, включая удаленные
     * @return список валют, отсортированных по позиции (счета с позицией 0 в конце)
     */
    @Query("SELECT * FROM currencies ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Currency>> getAll();

    /**
     * Получает все активные валюты
     * @return список активных валют, отсортированных по позиции
     */
    @Query("SELECT * FROM currencies WHERE deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Currency>> getAllActive();

    /**
     * Получает все удаленные валюты
     * @return список удаленных валют, отсортированных по позиции
     */
    @Query("SELECT * FROM currencies WHERE deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Currency>> getAllDeleted();

    /**
     * Получает валюту по ID (включая удаленные)
     * @param id ID валюты
     * @return валюта с указанным ID
     */
    @Query("SELECT * FROM currencies WHERE id = :id")
    LiveData<Currency> getById(int id);
        
    /**
     * Получает валюту по позиции (включая удаленные)
     * @param position позиция валюты
     * @return валюта с указанной позицией
     */
    @Query("SELECT * FROM currencies WHERE position = :position")
    LiveData<Currency> getByPosition(int position);
    
    /**
     * Получает валюту по названию (включая удаленные)
     * @param title название валюты
     * @return валюта с указанным названием
     */
    @Query("SELECT * FROM currencies WHERE title = :title")
    LiveData<Currency> getByTitle(String title);

    /**
     * Получает максимальную позицию среди валют
     * @return максимальная позиция или 0, если валют нет
     */
    @Query("SELECT COALESCE(MAX(position), 0) FROM currencies")
    int getMaxPosition();

    /**
     * Вставляет новую валюту в базу данных
     * @param currency валюта для вставки
     * @return ID вставленной валюты или -1 при конфликте по title
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Currency currency);
    
    /**
     * Получает валюты по подстроке в названии (включая удаленные)
     * @param searchQuery подстрока для поиска
     * @return список валют, содержащих подстроку в названии
     */
    @Query("SELECT * FROM currencies WHERE title LIKE '%' || :searchQuery || '%' ORDER BY position ASC")
    LiveData<List<Currency>> searchByTitle(String searchQuery);
    
    /**
     * Сдвигает позиции валют вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE currencies SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции валют вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE currencies SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующую валюту в базе данных
     * @param currency валюта для обновления
     */
    @Update
    void update(Currency currency);     

    /**
     * выравнивает позиции валют после удаления, игнорирует записи с position = 0
     * Устраняет разрывы в позициях
     */
    @Query("UPDATE currencies SET position = position - 1 WHERE position > 1 AND position != 0")
    void updatePositionsAfterDelete();

} 