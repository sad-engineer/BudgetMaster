// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.dao.CategoryDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository класс для работы с Category Entity
 */
public class CategoryRepository {
    
    private final CategoryDao categoryDao;
    private final ExecutorService executorService;
    
    public CategoryRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.categoryDao = database.categoryDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    // Получить все активные категории
    public LiveData<List<Category>> getAllActiveCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getAllActiveCategories();
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить категории по типу
    public LiveData<List<Category>> getCategoriesByType(int operationType) {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getCategoriesByType(operationType);
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить категории по родительской категории
    public LiveData<List<Category>> getCategoriesByParent(int parentId) {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getCategoriesByParent(parentId);
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить корневые категории
    public LiveData<List<Category>> getRootCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getRootCategories();
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить категорию по ID
    public LiveData<Category> getCategoryById(int id) {
        MutableLiveData<Category> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Category category = categoryDao.getCategoryById(id);
            liveData.postValue(category);
        });
        return liveData;
    }
    
    // Получить категорию по названию
    public LiveData<Category> getCategoryByName(String name) {
        MutableLiveData<Category> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Category category = categoryDao.getCategoryByName(name);
            liveData.postValue(category);
        });
        return liveData;
    }
    
    // Получить категорию по позиции
    public LiveData<Category> getCategoryByPosition(int position) {
        MutableLiveData<Category> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Category category = categoryDao.getCategoryByPosition(position);
            liveData.postValue(category);
        });
        return liveData;
    }
    
    // Вставить новую категорию
    public void insertCategory(Category category, String createdBy) {
        executorService.execute(() -> {
            category.setCreateTime(LocalDateTime.now());
            category.setCreatedBy(createdBy);
            category.setUpdateTime(LocalDateTime.now());
            category.setUpdatedBy(createdBy);
            categoryDao.insertCategory(category);
        });
    }
    
    // Обновить категорию
    public void updateCategory(Category category, String updatedBy) {
        executorService.execute(() -> {
            category.setUpdateTime(LocalDateTime.now());
            category.setUpdatedBy(updatedBy);
            categoryDao.updateCategory(category);
        });
    }
    
    // Удалить категорию (soft delete)
    public void deleteCategory(int categoryId, String deletedBy) {
        executorService.execute(() -> {
            categoryDao.softDeleteCategory(categoryId, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Удалить категорию по названию
    public void deleteCategoryByName(String name, String deletedBy) {
        executorService.execute(() -> {
            categoryDao.softDeleteCategoryByName(name, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Получить все удаленные категории
    public LiveData<List<Category>> getAllDeletedCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getAllDeletedCategories();
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить максимальную позицию
    public LiveData<Integer> getMaxPosition() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer maxPos = categoryDao.getMaxPosition();
            liveData.postValue(maxPos != null ? maxPos : 0);
        });
        return liveData;
    }
    
    // Восстановить категорию
    public void restoreCategory(int categoryId, String updatedBy) {
        executorService.execute(() -> {
            categoryDao.restoreCategory(categoryId, LocalDateTime.now().toString(), updatedBy);
        });
    }
    
    // Получить иерархию категорий
    public LiveData<List<Category>> getCategoryHierarchy() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Category> categories = categoryDao.getCategoryHierarchy();
            liveData.postValue(categories);
        });
        return liveData;
    }
    
    // Получить количество активных категорий
    public LiveData<Integer> getActiveCategoriesCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = categoryDao.getActiveCategoriesCount();
            liveData.postValue(count);
        });
        return liveData;
    }
} 