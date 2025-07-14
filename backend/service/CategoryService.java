package service;

import model.Category;
import repository.CategoryRepository;
import validator.CategoryValidator;
import constants.ServiceConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с категориями
 */
public class CategoryService {
    /**
     * Репозиторий для работы с категориями
     */
    private final CategoryRepository categoryRepository;

     /**
     * Пользователь, выполняющий операции
     */
    private final String user;
    
    /**
     * Конструктор для сервиса
     * @param categoryRepository репозиторий для работы с категориями
     * @param user пользователь, выполняющий операции
     */
    public CategoryService(CategoryRepository categoryRepository, String user) {
        this.categoryRepository = categoryRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public CategoryService(String user) {
        this.categoryRepository = new CategoryRepository(ServiceConstants.DEFAULT_DATABASE_NAME);
        this.user = user;
    }

    /**
     * Изменяет порядок категории с переупорядочиванием других категорий
     * @param category категория для изменения позиции
     * @param newPosition новая позиция
     * @return категория с новой позицией
     */
    public Category changePosition(Category category, int newPosition) {
        int oldPosition = category.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return category;
        }
        
        // Получаем все категории для переупорядочивания
        List<Category> allCategories = getAll();
        
        // Проверяем, что новая позиция валидна
        if (newPosition < 1 || newPosition > allCategories.size()) {
            throw new IllegalArgumentException(ServiceConstants.ERROR_POSITION_OUT_OF_RANGE + allCategories.size());
        }
        
        // Переупорядочиваем позиции
        if (oldPosition < newPosition) {
            // Двигаем категорию вниз: сдвигаем категории между старой и новой позицией вверх
            for (Category c : allCategories) {
                if (c.getId() != category.getId() && 
                    c.getPosition() > oldPosition && 
                    c.getPosition() <= newPosition) {
                    c.setPosition(c.getPosition() - 1);
                    c.setUpdateTime(LocalDateTime.now());
                    c.setUpdatedBy(user);
                    categoryRepository.update(c);
                }
            }
        } else {
            // Двигаем категорию вверх: сдвигаем категории между новой и старой позицией вниз
            for (Category c : allCategories) {
                if (c.getId() != category.getId() && 
                    c.getPosition() >= newPosition && 
                    c.getPosition() < oldPosition) {
                    c.setPosition(c.getPosition() + 1);
                    c.setUpdateTime(LocalDateTime.now());
                    c.setUpdatedBy(user);
                    categoryRepository.update(c);
                }
            }
        }
        
        // Устанавливаем новую позицию для целевой категории
        category.setPosition(newPosition);
        category.setUpdateTime(LocalDateTime.now());    
        category.setUpdatedBy(user);
        return categoryRepository.update(category);
    }

    /**
     * Изменяет порядок категории с переупорядочиванием других категорий
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     * @return категория с новой позицией
     */
    public Category changePosition(int oldPosition, int newPosition) {   
        Optional<Category> categoryOpt = getById(oldPosition);
        if (categoryOpt.isPresent()) {
            return changePosition(categoryOpt.get(), newPosition);
        }
        return null;
    }

    /**
     * Создает новую категорию
     * @param title title категории
     * @return созданная категория
     */
    public Category create(String title) {
        Category newCategory = new Category();
        newCategory.setTitle(title);
        int nextPosition = categoryRepository.getMaxPosition() + 1;
        newCategory.setPosition(nextPosition);
        newCategory.setCreateTime(LocalDateTime.now());
        newCategory.setCreatedBy(user);
        newCategory.setUpdateTime(LocalDateTime.now());
        newCategory.setUpdatedBy(user);

        // Валидация категории
        CategoryValidator.validateForCreate(newCategory);
        
        return categoryRepository.save(newCategory);
    }

    /**
     * Удаляет категорию по id
     * @param id id категории
     * @return true, если удаление успешно
     */
    public boolean delete(int id) {
        return categoryRepository.deleteById(id, user);
    }

    /**
     * Удаляет категорию по title
     * @param title title категории
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        return categoryRepository.deleteByTitle(title, user);
    }

    /**
     * Получает все категории
     * @return список категорий
     */
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    /**
     * Получает все категории по operation_type 
     * @param operation_type тип операции
     * @return список категорий
     */
    public List<Category> getAllByOperationType(int operation_type) {
        return categoryRepository.findAllByOperationType(operation_type);
    }

    /**
     * Получает все категории по type 
     * @param type тип категории
     * @return список категорий
     */
    public List<Category> getAllByType(int type) {
        return categoryRepository.findAllByType(type);
    }

    /**
     * Получает все категории по parent_id 
     * @param parent_id id родительской категории
     * @return список категорий
     */
    public List<Category> getAllByParentId(int parent_id) {
        return categoryRepository.findAllByParentId(parent_id);
    }

    /**
     * Получает категорию по ID
     * @param id ID категории
     * @return категория
     */
    public Optional<Category> getById(int id) { 
        return categoryRepository.findById(id);
    }

    /**
     * Получает категорию по title
     * @param title title категории
     * @return категория
     */
    public Optional<Category> getByTitle(String title) {
        return categoryRepository.findByTitle(title);
    }
   

    /**
     * Проверка категории на удаление
     * @param category класс категории
     * @return true, если категория удалена
     */
    public boolean isCategoryDeleted(Category category) {
        return category.getDeleteTime() != null;
    }

    /**
     * Восстанавливает категорию
     * @param restoredCategory категория
     * @return категория
     */
    public Category restore(Category restoredCategory) {
        restoredCategory.setDeleteTime(null);
        restoredCategory.setDeletedBy(null);
        restoredCategory.setUpdateTime(LocalDateTime.now());
        restoredCategory.setUpdatedBy(user);
        return categoryRepository.update(restoredCategory);
    }

    /**
     * Восстанавливает категорию по id
     * @param id id категории
     * @return категория или null, если категория не найдена
     */
    public Category restore(int id) {
        Optional<Category> categoryOpt = getById(id);
        if (categoryOpt.isPresent()) {
            return restore(categoryOpt.get());
        }
        return null;
    }   

    /**
     * Устанавливает нового пользователя для операций
     * @param newUser новый пользователь
     */
    public void setUser(String newUser) {
        // Обратите внимание: поле user final, поэтому нужно создать новый экземпляр сервиса
        // или использовать другой подход для смены пользователя
        throw new UnsupportedOperationException(ServiceConstants.ERROR_CANNOT_CHANGE_USER + "CategoryService");
    }
} 