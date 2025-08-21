package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import java.time.LocalDateTime;

import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Category
 */
public class CategoryService {
    private static final String TAG = "CategoryService";
    
    private final CategoryRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final ServiceConstants constants;
    
    public CategoryService(Context context, String user) {
        this.repo = new CategoryRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.constants = new ServiceConstants();
    }

    /**
     * Изменить позицию категории
     * @param category категория
     * @param newPosition новая позиция
     */
    public void changePosition(Category category, int newPosition) {
        executorService.execute(() -> {
            changePositionInTransaction(category, newPosition);
        });
    }
    
    /**
     * Транзакция для изменения позиции категории
     * @param category категория
     * @param newPosition новая позиция
     */
    @Transaction
    private void changePositionInTransaction(Category category, int newPosition) {
        int oldPosition = category.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return;
        }
        
        // Используем методы сдвига позиций из Repository
        if (oldPosition < newPosition) {
            repo.shiftPositionsDown(oldPosition);
            repo.shiftPositionsUp(newPosition + 1);
        } else {
            repo.shiftPositionsUp(newPosition);
            repo.shiftPositionsDown(oldPosition);
        }
        
        // Устанавливаем новую позицию для текущего счета
        category.setPosition(newPosition);
        repo.update(category);
    }
    
    /**
     * Изменить позицию категории по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        Category category = repo.getByPosition(oldPosition).getValue();
        if (category != null) {
            changePosition(category, newPosition);
        }
    }
    
    /**
     * Изменить позицию категории по названию
     * @param title название категории
     * @param newPosition новая позиция
     */
    public void changePosition(String title, int newPosition) {
        Category category = repo.getByTitle(title).getValue();
        if (category != null) {
            changePosition(category, newPosition);
        }
    }

    /**
     * Создать новый категорию
     * @param title название категории
     * @param operationType тип операции
     * @param type тип категории
     * @param parentId ID родителя
     */
    public void create(String title, Integer operationType, Integer type, Integer parentId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        executorService.execute(() -> {
            try {
                // Создаем переменные ВНУТРИ lambda с проверкой на null
                int finalOperationType = (operationType != null) ? operationType : constants.DEFAULT_CATEGORY_OPERATION_TYPE;
                int finalType = (type != null) ? type : constants.DEFAULT_CATEGORY_TYPE;
                int finalParentId = (parentId != null) ? parentId : constants.DEFAULT_PARENT_CATEGORY_ID;

                // Проверяем, если тип операции не передан, устанавливаем значение по умолчанию
                if (operationType == null || operationType <= 0) {
                    Log.d(TAG, "Для категории " + title + " не передано значение типа операции, устанавливаем значение по умолчанию");
                    finalOperationType = constants.DEFAULT_CATEGORY_OPERATION_TYPE;
                }

                // Проверяем, если тип категории не передан, устанавливаем значение по умолчанию
                if (type == null || type <= 0) {
                    Log.d(TAG, "Для категории " + title + " не передано значение типа категории, устанавливаем значение по умолчанию");
                    finalType = constants.DEFAULT_CATEGORY_TYPE;
                }

                // Проверяем, если ID родителя не передан, устанавливаем значение по умолчанию
                if (parentId == null || parentId <= 0) {
                    Log.d(TAG, "Для категории " + title + " не передано значение ID родителя, устанавливаем значение по умолчанию");
                    finalParentId = constants.DEFAULT_PARENT_CATEGORY_ID;
                }

                createCategoryInTransaction(title, finalOperationType, finalType, finalParentId);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при создании категории: " + e.getMessage(), e);
            }
        });
    }   

    /**
     * Транзакция для создания новой категории
     * @param title название категории
     * @param operationType тип операции
     * @param type тип категории
     * @param parentId ID родителя
     */
    @Transaction
    private void createCategoryInTransaction(String title, int operationType, int type, int parentId) {
        Log.d(TAG, "🔄 Запрос на создание категории: " + title);
        Category category = new Category();
        category.setTitle(title);
        category.setOperationType(operationType);
        category.setType(type);
        category.setParentId(parentId);
        category.setPosition(repo.getMaxPosition() + 1);
        category.setCreateTime(LocalDateTime.now());
        category.setCreatedBy(user);
        repo.insert(category);
        Log.d(TAG, "✅ Категория " + title + " успешно создана");
    }

    /**
     * Создать новый категорию со значениями по умолчанию
     * @param title название категории
     */
    public void create(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        executorService.execute(() -> {
            try {
                String trimmedTitle = title.trim();
                create(trimmedTitle, null, null, null);                
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при создании категории '" + title + "': " + e.getMessage(), e);
            }
        });
    }

     /**
     * Удалить категорию (полное удаление - удаление строки из БД)
     * @param category категория
     */
    private void delete(Category category) {
        if (category == null) {
            Log.e(TAG, "❌ Категория не найдена для удаления. Удаление было отменено");
            return;
        }
        executorService.execute(() -> {
            try {
                deleteCategoryInTransaction(category);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при удалении категории '" + category.getTitle() + "': " + e.getMessage(), e);
            }
        });
    }     
    
    /**
     * Транзакция для удаления категории
     * @param category категория
     */
    @Transaction
    private void deleteCategoryInTransaction(Category category) {
        Log.d(TAG, "🔄 Запрос на удаление категории: " + category.getTitle());
        int deletedPosition = category.getPosition();
        repo.delete(category);
        Log.d(TAG, "✅ Категория " + category.getTitle() + " успешно удалена");
    }

    /**
     * Удалить категорию (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param category категория
     */
    public void delete(boolean softDelete, Category category) {
        if (softDelete) {
            softDelete(category);
        } else {
            delete(category);
        }
    }

    /**
     * Получить все категории
     * @param filter фильтр для выборки категорий
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    }
    
    /**
     * Получить все категории (включая удаленные)
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAll() {
        return repo.getAll();
    }

    /**
     * Получить все категории по типу операции
     * @param operationType тип операции
     * @param filter фильтр для выборки категорий
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByOperationType(int operationType, EntityFilter filter) { 
        return repo.getAllByOperationType(operationType, filter);
    }
    
    /**
     * Получить все категории по ID родителя
     * @param parentId ID родителя
     * @param filter фильтр для выборки категорий       
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByParentId(int parentId, EntityFilter filter) {
        return repo.getAllByParentId(parentId, filter);
    }
    
    /**
     * Получить все категории по типу
     * @param type тип категории
     * @param filter фильтр для выборки категорий
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByType(String type, EntityFilter filter) {
        return repo.getAllByType(type, filter);
    }   

    /**
     * Получить категорию по ID
     * @param id ID категории
     * @return LiveData с категорией
     */
    public LiveData<Category> getById(int id) {
        return repo.getById(id);
    }

    /**
     * Получить категорию по названию
     * @param title название категории
     * @return LiveData с категорией
     */
    public LiveData<Category> getByTitle(String title) {
        return repo.getByTitle(title);
    }

    /**
     * Восстановить удаленную категорию (soft delete)
     * @param deletedCategory удаленная категория
     */
    public void restore(Category deletedCategory) {
        executorService.execute(() -> {
            try {
                restoreCategoryInTransaction(deletedCategory);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при восстановлении категории: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для восстановления категории
     * @param deletedCategory удаленная категория
     */
    @Transaction
    private void restoreCategoryInTransaction(Category deletedCategory) {
        if (deletedCategory != null) {
            Log.d(TAG, "🔄 Запрос на восстановление категории " + deletedCategory.getTitle());
            deletedCategory.setPosition(repo.getMaxPosition() + 1);
            deletedCategory.setDeleteTime(null);
            deletedCategory.setDeletedBy(null);
            deletedCategory.setUpdateTime(LocalDateTime.now());
            deletedCategory.setUpdatedBy(user);
            repo.update(deletedCategory);
            Log.d(TAG, "✅ Категория " + deletedCategory.getTitle() + " успешно восстановлена");
        } else {
            Log.e(TAG, "❌ Категория не найдена для восстановления");
        }
    }

    /**
     * Удалить категорию (soft delete)
     * @param category категория
     */
    private void softDelete(Category category) {
        if (category == null) {
            Log.e(TAG, "❌ Категория не найдена для soft delete. Удаление было отменено");
            return;
        }   
        executorService.execute(() -> {
            try {
                softDeleteCategoryInTransaction(category);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при soft delete категории: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Транзакция для удаления категории (soft delete)
     * @param category категория
     */
    @Transaction
    private void softDeleteCategoryInTransaction(Category category) {
        Log.d(TAG, "🔄 Запрос на softDelete категории " + category.getTitle());
        int deletedPosition = category.getPosition();
        category.setPosition(0);
        category.setDeleteTime(LocalDateTime.now());
        category.setDeletedBy(user);
        repo.update(category);
        // Пересчитываем позиции после soft delete
        repo.shiftPositionsDown(deletedPosition);
        Log.d(TAG, "✅ Категория " + category.getTitle() + " успешно soft deleted");
    }

    /**
     * Обновить категорию
     * @param category категория
     */
    public void update(Category category) {
        if (category == null) {
            Log.e(TAG, "❌ Категория не найдена для обновления. Обновление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на обновление категории " + category.getTitle());
                category.setUpdateTime(LocalDateTime.now());
                category.setUpdatedBy(user);
                repo.update(category);
                Log.d(TAG, "✅ Запрос на обновление категории " + category.getTitle() + " успешно отправлен");
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при обновлении категории " + category.getTitle() + ": " + e.getMessage(), e);
            }
        });
    }

    /**
     * Получить количество категорий
     * @param filter фильтр для выборки категорий
     * @return количество категорий
     */
    public int getCount(EntityFilter filter) {
        return repo.getCount(filter);
    }
    
    /**
     * Получить общее количество категорий (включая удаленные)
     * @return общее количество категорий
     */
    public int getCount() {
        return repo.getCount();
    }
    
    /**
     * Получить все дочерние категории для заданной категории (включая вложенные)
     * @param categoryId ID категории
     * @param filter фильтр для выборки категорий
     * @return LiveData со списком всех дочерних категорий
     */
    public LiveData<List<Category>> getAllDescendants(int categoryId, EntityFilter filter) {
        return repo.getAllDescendants(categoryId, filter);
    }
    
    /**
     * Закрыть ExecutorService
     * @deprecated Используйте ThreadManager.shutdown() для централизованного управления
     */
    @Deprecated
    public void shutdown() {
        // Не закрываем ExecutorService здесь, так как он общий
        // Используйте ThreadManager.shutdown() при завершении приложения
    }
}   