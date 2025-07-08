package service;

import model.Category;
import repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        // Проверяем, есть ли удаленная запись с таким же title, и восстанавливаем её
        Optional<Integer> deletedId = categoryRepository.findDeletedByTitle(category.getTitle());
        if (deletedId.isPresent()) {
            // Восстанавливаем удаленную запись
            categoryRepository.restore(deletedId.get());
            
            // Получаем восстановленную запись
            Optional<Category> restoredCategory = categoryRepository.findById(deletedId.get());
            if (restoredCategory.isPresent()) {
                Category restored = restoredCategory.get();
                
                // Обновляем данные восстановленной записи
                restored.setUpdateTime(LocalDateTime.now());
                restored.setUpdatedBy(category.getUpdatedBy());
                
                // Обновляем запись в БД
                categoryRepository.update(restored);
                
                // Устанавливаем ID восстановленной записи
                category.setId(restored.getId());
                category.setPosition(restored.getPosition());
                
                return category;
            }
        }
        
        // Удаленной записи не найдено, продолжаем обычное сохранение
        // Автоматически устанавливаем позицию, если она не установлена (равна 0)
        if (category.getPosition() == 0) {
            category.setPosition(categoryRepository.getNextPosition());
        }
        
        // Сохраняем новую запись
        Category savedCategory = categoryRepository.save(category);
        
        // Нормализуем позиции после сохранения
        categoryRepository.normalizePositions();
        
        return savedCategory;
    }

    public Optional<Category> getCategoryById(int id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category updateCategory(Category category) {
        // Корректируем позиции, если это необходимо перед обновлением
        categoryRepository.adjustPositionsForUpdate(category);
        
        // Обновляем запись
        Category updatedCategory = categoryRepository.update(category);
        
        // Нормализуем позиции после обновления
        categoryRepository.normalizePositions();
        
        return updatedCategory;
    }

    public boolean deleteCategory(int id) {
        return categoryRepository.delete(id);
    }

    public boolean deleteCategory(int id, String deletedBy) {
        return categoryRepository.delete(id, deletedBy);
    }

    public boolean restoreCategory(int id) {
        boolean restored = categoryRepository.restore(id);
        if (restored) {
            // Нормализуем позиции после восстановления
            categoryRepository.normalizePositions();
        }
        return restored;
    }

    public List<Category> getDeletedCategories() {
        return categoryRepository.findDeleted();
    }

    /**
     * Нормализует позиции всех активных категорий
     */
    public void normalizePositions() {
        categoryRepository.normalizePositions();
    }
} 