package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.sadengineer.budgetmaster.backend.dao.CategoryDao;

import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;

import java.util.List;

/**
 * Repository класс для работы с Category Entity
 */
public class CategoryRepository {

    private final CategoryDao dao;
    
    public CategoryRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.dao = database.categoryDao();
    }

    /**
     * Получить все категории по фильтру
     * @param filter фильтр для выборки категорий (ACTIVE, DELETED, ALL)        
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAll(EntityFilter filter) {
        return dao.getAll(filter);
    }
    
    /**
     * Получить все категории по типу операции
     * @param operationType тип операции
     * @param filter фильтр для выборки категорий (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByOperationType(int operationType, EntityFilter filter) {
        return dao.getAllByOperationType(operationType, filter);
    }

    /**
     * Получить все категории по ID родителя по фильтру
     * @param parentId ID родителя
     * @param filter фильтр для выборки категорий (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByParentId(int parentId, EntityFilter filter) {
        return dao.getAllByParentId(parentId, filter);
    }

    /**
     * Получить все категории по типу
     * @param type тип категории
     * @param filter фильтр для выборки категорий (ACTIVE, DELETED, ALL)
     * @return LiveData со списком всех категорий
     */
    public LiveData<List<Category>> getAllByType(String type, EntityFilter filter) {
        return dao.getAllByType(type, filter);
    }

    /**
     * Получить категорию по ID (включая удаленные)
     * @param id ID категории
     * @return LiveData с категорией
     */
    public LiveData<Category> getById(int id) {
        return dao.getById(id);
    }
    
    /**
     * Получить категорию по названию (включая удаленные)
     * @param title название категории
     * @return LiveData с категорией
     */
    public LiveData<Category> getByTitle(String title) {
        return dao.getByTitle(title);
    }
    
    /**
     * Получить категорию по позиции (включая удаленные)
     * @param position позиция категории
     * @return LiveData с категорией
     */
    public LiveData<Category> getByPosition(int position) {
        return dao.getByPosition(position);
    }

    /**
     * Вставить новый категорию
     * @param category категория для вставки
     * @return ID вставленной категории
     */
    public long insert(Category category) {
        return dao.insert(category);
    }
    
    /**
     * Обновить категорию
     * @param category категория для обновления
     */
    public void update(Category category) {
        dao.update(category);
    }
    
    /**
     * Удалить категорию (полное удаление из БД)
     * @param category категория для удаления
     */
    public void delete(Category category) {
        dao.delete(category);
    }
    
    /**
     * Удалить все категории
     */
    public void deleteAll() {
        dao.deleteAll();
    }
    
    /**
     * Получить максимальную позицию среди всех категорий
     * @return максимальная позиция
     */
    public int getMaxPosition() {
        return dao.getMaxPosition();
    }
    
    /**
     * Сдвинуть позиции категорий вверх начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsUp(int fromPosition) {
        dao.shiftPositionsUp(fromPosition);
    }
    
    /**
     * Сдвинуть позиции категорий вниз начиная с указанной позиции
     * @param fromPosition позиция, с которой начинается сдвиг
     */
    public void shiftPositionsDown(int fromPosition) {
        dao.shiftPositionsDown(fromPosition);
    }

    /**
     * Получить количество категорий по фильтру
     * @param filter фильтр для выборки категорий (ACTIVE, DELETED, ALL)
     * @return количество категорий
     */
    public int getCount(EntityFilter filter) {
        return dao.count(filter);
    }
} 