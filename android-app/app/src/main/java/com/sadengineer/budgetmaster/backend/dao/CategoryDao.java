package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

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
    @Query("SELECT COUNT(*) FROM categories WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL'))")
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
    @Query("DELETE FROM categories")
    void deleteAll();
    
    /**
     * Получает все категории по фильтру
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAll(EntityFilter filter);

    /**
     * Получает все категории синхронно, включая удаленные
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    List<Category> getAllSync();

    /**
     * Получает все категории по типу операции по фильтру
     * @param operationType тип операции
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий с указанным типом операции, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE type = :operationType AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAllByOperationType(int operationType, EntityFilter filter);

    /**
     * Получает все дочерние категории для заданного родителя (включая вложенные) по фильтру
     * Использует рекурсивный CTE запрос для получения всех потомков
     * @param parentId ID родителя
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список всех дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM categories " +
           "  WHERE parentId = :parentId AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM categories c " +
           "  INNER JOIN descendants d ON c.parentId = d.id" +
           ") " +
           "SELECT * FROM descendants WHERE " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY position ASC")
    LiveData<List<Category>> getAllByParentId(int parentId, EntityFilter filter);
    
    /**
     * Получает все категории по типу по фильтру
     * @param type тип категории
     * @param filter фильтр (ACTIVE, DELETED, ALL)
     * @return список категорий с указанным типом, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE type = :type AND " +
           "((:filter = 'ACTIVE' AND deleteTime IS NULL) OR " +
           "(:filter = 'DELETED' AND deleteTime IS NOT NULL) OR " +
           "(:filter = 'ALL')) " +
           "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAllByType(String type, EntityFilter filter);

    /**
     * Получает категорию по ID (включая удаленные)
     * @param id ID категории
     * @return категория с указанным ID
     */
    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getById(int id);
        
    /**
     * Получает категорию по позиции (включая удаленные)
     * @param position позиция категории
     * @return категория с указанной позицией
     */
    @Query("SELECT * FROM categories WHERE position = :position")
    LiveData<Category> getByPosition(int position);
    
    /**
     * Получает категорию по названию (включая удаленные)
     * @param title название категории
     * @return категория с указанным названием
     */
    @Query("SELECT * FROM categories WHERE title = :title")
    LiveData<Category> getByTitle(String title);

    /**
     * Получает максимальную позицию среди категорий
     * @return максимальная позиция или null, если категорий нет
     */
    @Query("SELECT MAX(position) FROM categories")
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
    @Query("SELECT * FROM categories WHERE title = :searchQuery ORDER BY position ASC")
    LiveData<List<Category>> searchByTitle(String searchQuery);
    
    /**
     * Сдвигает позиции счетов вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE categories SET position = position - 1 WHERE position > :fromPosition")
    void shiftPositionsDown(int fromPosition);

    /**
     * Сдвигает позиции категорий вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    @Query("UPDATE categories SET position = position + 1 WHERE position >= :fromPosition")
    void shiftPositionsUp(int fromPosition);

    /**
     * Обновляет существующую категорию в базе данных
     * @param category категория для обновления
     */
    @Update
    void update(Category category);
} 