package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Category
 */
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ExecutorService executorService;
    private final String user;
    
    public CategoryService(Context context, String user) {
        this.categoryRepository = new CategoryRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }
    
    // Получить все активные категории
    public LiveData<List<Category>> getAllActiveCategories() {
        return categoryRepository.getAllActiveCategories();
    }
    
    // Получить категории по типу
    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryRepository.getCategoriesByType(type);
    }
    
    // Получить категории по родительской категории
    public LiveData<List<Category>> getCategoriesByParent(int parentId) {
        return categoryRepository.getCategoriesByParent(parentId);
    }
    
    // Получить корневые категории
    public LiveData<List<Category>> getRootCategories() {
        return categoryRepository.getRootCategories();
    }
    
    // Получить категорию по ID
    public LiveData<Category> getCategoryById(int id) {
        return categoryRepository.getCategoryById(id);
    }
    
    // Получить категорию по названию
    public LiveData<Category> getCategoryByName(String name) {
        return categoryRepository.getCategoryByName(name);
    }
    
    // Создать новую категорию
    public void createCategory(String name, String type, Integer parentId, String color, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setParentId(parentId);
        category.setColor(color);
        category.setIcon(icon);
        category.setDefault(false);
        category.setPosition(1); // TODO: Получить следующую позицию
        
        categoryRepository.insertCategory(category, user);
    }
    
    // Обновить категорию
    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category, user);
    }
    
    // Удалить категорию (soft delete)
    public void deleteCategory(int categoryId) {
        categoryRepository.deleteCategory(categoryId, user);
    }
    
    // Удалить категорию по названию
    public void deleteCategory(String name) {
        categoryRepository.deleteCategoryByName(name, user);
    }
    
    // Восстановить удаленную категорию
    public void restoreCategory(int categoryId) {
        executorService.execute(() -> {
            // Получаем удаленную категорию
            Category deletedCategory = categoryRepository.getCategoryById(categoryId).getValue();
            if (deletedCategory == null || !deletedCategory.isDeleted()) {
                return; // Категория не найдена или уже активна
            }
            
            // Очищаем поля удаления
            deletedCategory.setDeleteTime(null);
            deletedCategory.setDeletedBy(null);
            deletedCategory.setUpdateTime(LocalDateTime.now());
            deletedCategory.setUpdatedBy(user);
            
            // Обновляем категорию в базе
            categoryRepository.updateCategory(deletedCategory, user);
        });
    }
    
    // Изменить позицию категории (сложная логика)
    public void changePosition(Category category, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = category.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все активные категории для переупорядочивания
            List<Category> allCategories = categoryRepository.getAllActiveCategories().getValue();
            if (allCategories == null) return;
            
            // Проверяем, что новая позиция валидна
            int maxPosition = allCategories.size();
            if (newPosition < 1 || newPosition > maxPosition) {
                throw new IllegalArgumentException("Позиция вне диапазона: " + maxPosition);
            }
            
            // Переупорядочиваем позиции
            if (oldPosition < newPosition) {
                // Двигаем категорию вниз: сдвигаем категории между старой и новой позицией вверх
                for (Category c : allCategories) {
                    if (c.getId() != category.getId() && 
                        c.getPosition() > oldPosition && 
                        c.getPosition() <= newPosition) {
                        c.setPosition(c.getPosition() - 1);
                        categoryRepository.updateCategory(c, user);
                    }
                }
            } else {
                // Двигаем категорию вверх: сдвигаем категории между новой и старой позицией вниз
                for (Category c : allCategories) {
                    if (c.getId() != category.getId() && 
                        c.getPosition() >= newPosition && 
                        c.getPosition() < oldPosition) {
                        c.setPosition(c.getPosition() + 1);
                        categoryRepository.updateCategory(c, user);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущей категории
            category.setPosition(newPosition);
            categoryRepository.updateCategory(category, user);
        });
    }
    
    // Изменить позицию категории по старой позиции
    public void changePosition(int oldPosition, int newPosition) {
        executorService.execute(() -> {
            Category category = categoryRepository.getCategoryByPosition(oldPosition).getValue();
            if (category != null) {
                changePosition(category, newPosition);
            }
        });
    }
    
    // Изменить позицию категории по названию
    public void changePosition(String name, int newPosition) {
        executorService.execute(() -> {
            Category category = categoryRepository.getCategoryByName(name).getValue();
            if (category != null) {
                changePosition(category, newPosition);
            }
        });
    }
    
    // Получить или создать категорию
    public LiveData<Category> getOrCreateCategory(String name, String type, Integer parentId) {
        MutableLiveData<Category> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            Category existingCategory = categoryRepository.getCategoryByName(name).getValue();
            if (existingCategory != null) {
                liveData.postValue(existingCategory);
                return;
            }
            
            // Если не найден - создаем новый
            Category newCategory = new Category();
            newCategory.setName(name);
            newCategory.setType(type);
            newCategory.setParentId(parentId);
            newCategory.setColor("#FF0000"); // TODO: Получить цвет по умолчанию
            newCategory.setIcon("default_icon"); // TODO: Получить иконку по умолчанию
            newCategory.setDefault(false);
            newCategory.setPosition(1); // TODO: Получить следующую позицию
            
            categoryRepository.insertCategory(newCategory, user);
            liveData.postValue(newCategory);
        });
        return liveData;
    }
    
    // Получить или создать категорию с полными параметрами
    public LiveData<Category> getOrCreateCategory(String name, String type, Integer parentId, String color, String icon) {
        MutableLiveData<Category> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            Category existingCategory = categoryRepository.getCategoryByName(name).getValue();
            if (existingCategory != null) {
                // Проверяем, совпадают ли параметры
                if (existingCategory.getType().equals(type) && 
                    (existingCategory.getParentId() == null && parentId == null || 
                     existingCategory.getParentId() != null && existingCategory.getParentId().equals(parentId))) {
                    liveData.postValue(existingCategory);
                    return;
                }
                
                // Если параметры не совпадают, обновляем категорию
                existingCategory.setType(type);
                existingCategory.setParentId(parentId);
                existingCategory.setColor(color);
                existingCategory.setIcon(icon);
                categoryRepository.updateCategory(existingCategory, user);
                liveData.postValue(existingCategory);
                return;
            }
            
            // Если не найден - создаем новый
            Category newCategory = new Category();
            newCategory.setName(name);
            newCategory.setType(type);
            newCategory.setParentId(parentId);
            newCategory.setColor(color);
            newCategory.setIcon(icon);
            newCategory.setDefault(false);
            newCategory.setPosition(1); // TODO: Получить следующую позицию
            
            categoryRepository.insertCategory(newCategory, user);
            liveData.postValue(newCategory);
        });
        return liveData;
    }
    
    // Получить количество активных категорий
    public LiveData<Integer> getActiveCategoriesCount() {
        return categoryRepository.getActiveCategoriesCount();
    }
    
    // Валидация категории
    public boolean validateCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            return false;
        }
        if (category.getType() == null || category.getType().trim().isEmpty()) {
            return false;
        }
        if (!category.getType().equals("income") && !category.getType().equals("expense")) {
            return false;
        }
        return true;
    }
    
    // Проверить, является ли категория родительской
    public boolean isParentCategory(Category category) {
        return category.getParentId() == null;
    }
    
    // Проверить, является ли категория дочерней
    public boolean isChildCategory(Category category) {
        return category.getParentId() != null;
    }
    
    // Получить иерархию категорий
    public LiveData<List<Category>> getCategoryHierarchy() {
        return categoryRepository.getCategoryHierarchy();
    }
    
    // Получить подкатегории
    public LiveData<List<Category>> getSubcategories(int parentId) {
        return categoryRepository.getCategoriesByParent(parentId);
    }
    
    // Проверить, можно ли удалить категорию
    public boolean canDeleteCategory(int categoryId) {
        // TODO: Проверить, есть ли операции с этой категорией
        return true;
    }
} 