
package com.sadengineer.budgetmaster.backend.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.entity.Category;

import java.util.List;

/**
 * Data Access Object для работы с Category Entity
 */
@Dao
public interface CategoryDao {
    /**
     * Количество активных категорий
     * @return количество активных категорий
     */
    @Query("SELECT COUNT(*) FROM categories WHERE deleteTime IS NULL")
    int countActive();

    /**
     * Количество удаленных категорий
     * @return количество удаленных категорий
     */
    @Query("SELECT COUNT(*) FROM categories WHERE deleteTime IS NOT NULL")
    int countDeleted();

    /**
     * Общее количество категорий (включая удаленные)
     * @return общее количество категорий
     */
    @Query("SELECT COUNT(*) FROM categories")
    int count();

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
     * Получает все категории, включая удаленные
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAll();

    /**
     * Получает все категории синхронно, включая удаленные
     * @return список категорий, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    List<Category> getAllSync();

    /**
     * Получает все активные категории
     * @return список активных категорий, отсортированных по позиции
     */
    @Query("SELECT * FROM categories WHERE deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllActive();

    /**
     * Получает все удаленные категории
     * @return список удаленных категорий, отсортированных по позиции
     */
    @Query("SELECT * FROM categories WHERE deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllDeleted();

    /**
     * Получает все категории по типу операции (включая удаленные)
     * @param operationType тип операции
     * @return список категорий с указанным типом операции, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE operationType = :operationType ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAllByOperationType(int operationType);

    /**
     * Получает все активные категории по типу операции
     * @param operationType тип операции
     * @return список категорий с указанным типом операции, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE operationType = :operationType AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllActiveByOperationType(int operationType);

    /**
     * Получает все удаленные категории по типу операции
     * @param operationType тип операции
     * @return список категорий с указанным типом операции, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE operationType = :operationType AND deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllDeletedByOperationType(int operationType);

    /**
     * Получает все категории по ID родителя (включая удаленные)
     * @param parentId ID родителя
     * @return список категорий с указанным ID родителя, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAllByParentId(int parentId);

    /**
     * Получает все активные категории по ID родителя
     * @param parentId ID родителя
     * @return список категорий с указанным ID родителя, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllActiveByParentId(int parentId);

    /**
     * Получает все удаленные категории по ID родителя
     * @param parentId ID родителя
     * @return список категорий с указанным ID родителя, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllDeletedByParentId(int parentId);

    /**
     * Получает все категории по типу (включая удаленные)
     * @param type тип категории
     * @return список категорий с указанным типом, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC")
    LiveData<List<Category>> getAllByType(String type);

    /**
     * Получает все активные категории по типу
     * @param type тип категории
     * @return список категорий с указанным типом, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE type = :type AND deleteTime IS NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllActiveByType(String type);

    /**
     * Получает все удаленные категории по типу
     * @param type тип категории
     * @return список категорий с указанным типом, отсортированных по позиции (категории с позицией 0 в конце)
     */
    @Query("SELECT * FROM categories WHERE type = :type AND deleteTime IS NOT NULL ORDER BY position ASC")
    LiveData<List<Category>> getAllDeletedByType(String type);

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
    @Query("SELECT * FROM categories WHERE title LIKE '%' || :searchQuery || '%' ORDER BY position ASC")
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
    
    /**
     * Получает все дочерние категории для заданной категории (включая вложенные)
     * Использует рекурсивный CTE запрос для получения всех потомков
     * @param categoryId ID категории
     * @return список всех дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM categories " +
           "  WHERE parentId = :categoryId " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM categories c " +
           "  INNER JOIN descendants d ON c.parentId = d.id" +
           ") " +
           "SELECT * FROM descendants ORDER BY position ASC")
    LiveData<List<Category>> getAllDescendants(int categoryId);
    
    /**
     * Получает все активные дочерние категории для заданной категории (включая вложенные)
     * @param categoryId ID категории
     * @return список всех активных дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM categories " +
           "  WHERE parentId = :categoryId AND deleteTime IS NULL " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM categories c " +
           "  INNER JOIN descendants d ON c.parentId = d.id " +
           "  WHERE c.deleteTime IS NULL" +
           ") " +
           "SELECT * FROM descendants ORDER BY position ASC")
    LiveData<List<Category>> getAllActiveDescendants(int categoryId);
    
    /**
     * Получает все удаленные дочерние категории для заданной категории (включая вложенные)
     * @param categoryId ID категории
     * @return список всех удаленных дочерних категорий, отсортированных по позиции
     */
    @Query("WITH RECURSIVE descendants AS (" +
           "  SELECT * " +
           "  FROM categories " +
           "  WHERE parentId = :categoryId AND deleteTime IS NOT NULL " +
           "  UNION ALL " +
           "  SELECT c.* " +
           "  FROM categories c " +
           "  INNER JOIN descendants d ON c.parentId = d.id " +
           "  WHERE c.deleteTime IS NOT NULL" +
           ") " +
           "SELECT * FROM descendants ORDER BY position ASC")
    LiveData<List<Category>> getAllDeletedDescendants(int categoryId);
} 