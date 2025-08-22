
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.constants.SqlConstants;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;

/**
 * Data Access Object для работы с Currency Entity
 */
@Dao
public interface CurrencyDao {
    // Константы для SQL запросов
    private final String TABLE = SqlConstants.TABLE_CURRENCIES;
    private final String ENTITY_FILTER_CONDITION = SqlConstants.ENTITY_FILTER_CONDITION;
    private final String ACTIVE_CONDITION = SqlConstants.ACTIVE_CONDITION;
    private final String DELETED_CONDITION = SqlConstants.DELETED_CONDITION;
    private final String ID_CONDITION = SqlConstants.ID_CONDITION;
    private final String POSITION_CONDITION = SqlConstants.POSITION_CONDITION;
    private final String TITLE_CONDITION = SqlConstants.TITLE_CONDITION;
    private final String SHORT_NAME_CONDITION = SqlConstants.SHORT_NAME_CONDITION;
    private final String SORT_CONDITION = SqlConstants.POSITION_SORT_CONDITION;
    private final String SORT_CONDITION_0_END = SqlConstants.POSITION_SORT_CONDITION_0_END;

    /**
     * Количество активных валют
     * @return количество активных валют
     */
    @Query("SELECT COUNT(*) FROM " + TABLE + " WHERE " + ENTITY_FILTER_CONDITION)
    int countActive();

    /**
     * Количество удаленных валют
     * @return количество удаленных валют
     */
    @Query("SELECT COUNT(*) FROM " + TABLE + " WHERE " + DELETED_CONDITION)
    int countDeleted();

    /**
     * Общее количество валют (включая удаленные)
     * @return общее количество валют
     */
    @Query("SELECT COUNT(*) FROM " + TABLE)
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
    @Query("DELETE FROM " + TABLE)
    void deleteAll();
    
    /**
     * Получает все валюты, включая удаленные
     * @return список валют, отсортированных по позиции (счета с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE + " ORDER BY " + SORT_CONDITION_0_END)
    LiveData<List<Currency>> getAll();

    /**
     * Получает все активные валюты
     * @return список активных валют, отсортированных по позиции
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ACTIVE_CONDITION + " ORDER BY " + SORT_CONDITION)
    LiveData<List<Currency>> getAllActive();

    /**
     * Получает все удаленные валюты
     * @return список удаленных валют, отсортированных по позиции
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + DELETED_CONDITION + " ORDER BY " + SORT_CONDITION)
    LiveData<List<Currency>> getAllDeleted();

    /**
     * Получает валюту по ID (включая удаленные)
     * @param id ID валюты
     * @return валюта с указанным ID
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ID_CONDITION)
    LiveData<Currency> getById(int id);
    
    /**
     * Получает валюту по ID синхронно (включая удаленные)
     * @param id ID валюты
     * @return валюта с указанным ID
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + ID_CONDITION)
    Currency getByIdSync(int id);
        
    /**
     * Получает валюту по позиции (включая удаленные)
     * @param position позиция валюты
     * @return валюта с указанной позицией
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + POSITION_CONDITION)
    LiveData<Currency> getByPosition(int position);
    
    /**
     * Получает валюту по названию (включая удаленные)
     * @param title название валюты
     * @return валюта с указанным названием
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + TITLE_CONDITION)
    LiveData<Currency> getByTitle(String title);

    /**
     * Получает валюту по короткому имени (включая удаленные)
     * @param shortName короткое имя валюты
     * @return валюта с указанным коротким именем
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + SHORT_NAME_CONDITION)
    LiveData<Currency> getByShortName(String shortName);

    /**
     * Проверяет существование валюты с указанным названием
     * @param title название валюты
     * @return true если валюта существует, false если нет
     */
    @Query("SELECT EXISTS(SELECT 1 FROM " + TABLE + " WHERE " + TITLE_CONDITION + ")")
    boolean existsByTitle(String title);

    /**
     * Проверяет существование валюты с указанным коротким именем
     * @param shortName короткое имя валюты
     * @return true если валюта существует, false если нет
     */
    @Query("SELECT EXISTS(SELECT 1 FROM " + TABLE + " WHERE " + SHORT_NAME_CONDITION + ")")
    boolean existsByShortName(String shortName);

    /**
     * Проверяет существование валюты с указанным названием, исключая валюту по ID
     * @param title название валюты
     * @param excludeId ID валюты, которую нужно исключить из проверки
     * @return true если валюта существует, false если нет
     */
    @Query("SELECT EXISTS(SELECT 1 FROM " + TABLE + " WHERE " + TITLE_CONDITION + " AND id != :excludeId)")
    boolean existsByTitleExcludingId(String title, int excludeId);

    /**
     * Проверяет существование валюты с указанным коротким именем, исключая валюту по ID
     * @param shortName короткое имя валюты
     * @param excludeId ID валюты, которую нужно исключить из проверки
     * @return true если валюта существует, false если нет
     */
    @Query("SELECT EXISTS(SELECT 1 FROM " + TABLE + " WHERE " + SHORT_NAME_CONDITION + " AND id != :excludeId)")
    boolean existsByShortNameExcludingId(String shortName, int excludeId);

    /**
     * Получает максимальную позицию среди валют
     * @return максимальная позиция или 0, если валют нет
     */
    @Query("SELECT COALESCE(MAX(position), 0) FROM " + TABLE)
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
    @Query("SELECT * FROM " + TABLE + " WHERE " + TITLE_CONDITION + " ORDER BY " + SORT_CONDITION)
    LiveData<List<Currency>> searchByTitle(String searchQuery);

    /**
     * Получает валюты по подстроке в названии или коротком имени (включая удаленные)
     * @param searchQuery подстрока для поиска
     * @return список валют, содержащих подстроку в названии или коротком имени
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + TITLE_CONDITION + " OR " + SHORT_NAME_CONDITION + " ORDER BY " + SORT_CONDITION)
    LiveData<List<Currency>> searchByTitleOrShortName(String searchQuery);
    
    /**
     * Сдвигает позиции валют вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE + " SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции валют вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE + " SET position = position + 1 WHERE position >= :fromPosition")
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
    @Query("UPDATE " + TABLE + " SET position = position - 1 WHERE position > 1 AND position != 0")
    void updatePositionsAfterDelete();

} 