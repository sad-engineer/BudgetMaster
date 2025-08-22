
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Category;

import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TABLE_CATEGORIES;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ENTITY_FILTER_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ACTIVE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.DELETED_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.ID_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TITLE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.TYPE_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.PARENT_ID_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION;
import static com.sadengineer.budgetmaster.backend.constants.SqlConstants.POSITION_SORT_CONDITION_0_END;

import java.util.List;

/**
 * Data Access Object для работы с Category Entity
 */
@Dao
public interface CategoryDao {
    /**
     * Общее количество категорий по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return общее количество категорий по фильтру
     */
    @Query("SELECT COUNT(*) FROM " + TABLE_CATEGORIES + " WHERE " + ENTITY_FILTER_CONDITION)
    int count(EntityFilter filter);

    /**
     * Удаляет категорию из базы данных
     * @param category категория для удаления
     */
    @Delete
    void delete(Category category);
    
    /**
     * Удаляет все категории из базы данных
     */
    @Query("DELETE FROM " + TABLE_CATEGORIES)
    void deleteAll();
    
    /**
     * Получает все категории по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Category>> getAll(EntityFilter filter);

    /**
     * Получает все категории синхронно, включая удаленные
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE + " ORDER BY " + SORT_CONDITION_0_END)
    List<Category> getAllSync();

    /**
     * Получает все категории по типу операции по фильтру
     * @param operationType тип операции
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий с указанным типом операции, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE + " WHERE " + TYPE_CONDITION + " ORDER BY " + SORT_CONDITION_0_END)
    LiveData<List<Category>> getAllByOperationType(int operationType, EntityFilter filter);

    /**
     * Получает все категории по ID родителя по фильтру
     * @param parentId ID родителя
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий с указанным ID родителя, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + PARENT_ID_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Category>> getAllByParentId(int parentId, EntityFilter filter);

    /**
     * Получает все категории по типу по фильтру
     * @param type тип категории
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий с указанным типом, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + TYPE_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION_0_END)
    LiveData<List<Category>> getAllByType(String type, EntityFilter filter);

    /**
     * Получает категорию по ID (включая удаленные)
     * @param id ID категории
     * @return категория с указанным ID
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + ID_CONDITION)
    LiveData<Category> getById(int id);
        
    /**
     * Получает категорию по позиции (включая удаленные)
     * @param position позиция категории
     * @return категория с указанной позицией
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + POSITION_CONDITION)
    LiveData<Category> getByPosition(int position);
    
    /**
     * Получает категорию по названию (включая удаленные)
     * @param title название категории
     * @return категория с указанным названием
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + TITLE_CONDITION)
    LiveData<Category> getByTitle(String title);

    /**
     * Получает максимальную позицию среди категорий
     * @return максимальная позиция или null, если категорий нет
     */
    @Query("SELECT MAX(position) FROM " + TABLE_CATEGORIES)
    Integer getMaxPosition();

    /**
     * Вставляет новый категорию в базу данных
     * @param category категория для вставки
     * @return ID вставленной категории или -1 при конфликте по title
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Category category);
    
    /**
     * Получает категории по подстроке в названии (включая удаленные)
     * @param searchQuery подстрока для поиска
     * @return список категорий, содержащих подстроку в названии
     */
    @Query("SELECT * FROM " + TABLE_CATEGORIES + " WHERE " + TITLE_CONDITION + " ORDER BY " + POSITION_SORT_CONDITION)
    LiveData<List<Category>> searchByTitle(String searchQuery);
    
    /**
     * Сдвигает позиции счетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_CATEGORIES + " SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции категорий вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE " + TABLE_CATEGORIES + " SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующую категорию в базе данных
     * @param category категория для обновления
     */
    @Update
    void update(Category category);
    
    /**
     * Получает все дочерние категории для заданной категории (включая вложенные)
     * Использует рекурсивный CTE запрос для получения всех потомков
     * @param categoryId ID категории
     * @return список всех дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM " + TABLE_CATEGORIES + 
           "  WHERE parentId = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM " + TABLE_CATEGORIES + " c " +
           "  INNER JOIN descendants d ON c.parentId = d.id" +
           ") " +
           "SELECT * FROM descendants ORDER BY " + POSITION_SORT_CONDITION)
    LiveData<List<Category>> getAllDescendants(int categoryId);
    
    /**
     * Получает все активные дочерние категории для заданной категории (включая вложенные)
     * @param categoryId ID категории
     * @return список всех активных дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM " + TABLE_CATEGORIES + 
           "  WHERE parentId = :categoryId AND deleteTime IS NULL " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM " + TABLE_CATEGORIES + " c " +
           "  INNER JOIN descendants d ON c.parentId = d.id " +
           "  WHERE c.deleteTime IS NULL" +
           ") " +
           "SELECT * FROM descendants ORDER BY " + POSITION_SORT_CONDITION)
    LiveData<List<Category>> getAllActiveDescendants(int categoryId);
    
    /**
     * Получает все удаленные дочерние категории для заданной категории (включая вложенные)
     * @param categoryId ID категории
     * @return список всех удаленных дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM " + TABLE_CATEGORIES + 
           "  WHERE parentId = :categoryId AND deleteTime IS NOT NULL " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM " + TABLE_CATEGORIES + " c " +
           "  INNER JOIN descendants d ON c.parentId = d.id " +
           "  WHERE c.deleteTime IS NOT NULL " +
           ") " +
           "SELECT * FROM descendants ORDER BY " + POSITION_SORT_CONDITION)
    LiveData<List<Category>> getAllDeletedDescendants(int categoryId);
} 