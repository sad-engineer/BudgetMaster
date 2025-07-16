// -*- coding: utf-8 -*-
package service;

import model.Category;
import repository.CategoryRepository;
import validator.CategoryValidator;
import validator.BaseEntityValidator;
import validator.CommonValidator;
import constants.ServiceConstants;
import constants.ModelConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * Удаляет категорию по id
     * @param id id категории
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        CommonValidator.validateId(id);
        return categoryRepository.deleteById(id, user);
    }

    /**
     * Удаляет категорию по title
     * @param title название категории
     * @return true, если удаление успешно
     */
    public boolean delete(String title) {
        CommonValidator.validateCategoryTitle(title);
        return categoryRepository.deleteByTitle(title, user);
    }

    /**
     * Изменяет порядок категории с переупорядочиванием других категорий
     * @param category категория для изменения позиции
     * @param newPosition новая позиция
     * @return категория с новой позицией
     */
    public Category changePosition(Category category, int newPosition) {
        BaseEntityValidator.validate(category);
        CommonValidator.validatePositivePosition(newPosition);
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
     * @return категория с новой позицией. Если категория не найдена, возвращает null
     */
    public Category changePosition(int oldPosition, int newPosition) {
        CommonValidator.validatePositivePosition(oldPosition);
        Optional<Category> category = categoryRepository.findByPosition(oldPosition);
        if (category.isPresent()) {
            return changePosition(category.get(), newPosition);
        }
        return null;
    }

    /**
     * Изменяет порядок категории с переупорядочиванием других категорий
     * @param title название категории
     * @param newPosition новая позиция
     * @return категория с новой позицией. Если категория не найдена, возвращает null
     */
    public Category changePosition(String title, int newPosition) {
        CommonValidator.validateCategoryTitle(title);
        Optional<Category> categoryOpt = categoryRepository.findByTitle(title);
        if (categoryOpt.isPresent()) {
            return changePosition(categoryOpt.get(), newPosition);
        }
        return null;
    }    

    /**
     * Создает новую категорию без валидации названия категории (для внутреннего использования)
     * @param title название категории
     * @param operationType тип операций (1-расход, 2-доход)
     * @param type тип категории (0-родительская, 1-дочерняя)
     * @param parentId ID родительской категории (null для корневых категорий)
     * @return категория
     */
    private Category create(String title, int operationType, int type, Integer parentId) {
        Category newCategory = new Category();
        int nextPosition = categoryRepository.getMaxPosition() + 1;
        newCategory.setTitle(title);
        newCategory.setOperationType(operationType);
        newCategory.setType(type);
        newCategory.setParentId(parentId);
        newCategory.setPosition(nextPosition);
        newCategory.setCreateTime(LocalDateTime.now());
        newCategory.setCreatedBy(user);

        // Валидация категории
        CategoryValidator.validateForCreate(newCategory);

        return categoryRepository.save(newCategory);
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
     * Получает категорию по ID. 
     * Если категория с таким ID существует, возвращает ее.
     * Если категория с таким ID существует, но удалена, восстанавливает ее (удаляет информацию об удалении категории).
     * Если категория с таким ID не существует, вернет null.
     * @param id ID категории
     * @return категория
     */
    public Category get(Integer id) { 
        BaseEntityValidator.validatePositiveId(id, "ID категории");
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            Category categoryObj = category.get();
            if (isCategoryDeleted(categoryObj)) {
                return restore(categoryObj);
            }
            return categoryObj;
        }
        return null;
    }

    /**
     * Получает категорию по названию. 
     * Если категория с таким названием существует, возвращает ее.
     * Если категория с таким названием существует, но удалена, восстанавливает ее (удаляет информацию об удалении категории).
     * Если категория с таким названием не существует, возвращает null.
     * @param title название категории
     * @return категория
     */
    public Category get(String title) {
        CommonValidator.validateCategoryTitle(title);
        Optional<Category> category = categoryRepository.findByTitle(title);
        if (category.isPresent()) {
            Category categoryObj = category.get();
            if (isCategoryDeleted(categoryObj)) {
                return restore(categoryObj);
            }
            return categoryObj;
        }
        return null;
    }

    /**
     * Получает категорию по названию или создает новую с указанными параметрами.
     * Если категория с таким названием существует, и параметры совпадают с указанными, возвращает ее.
     * Если категория с таким названием существует, но параметры отличные от указанных, обновляет ее.
     * Если категория с таким названием существует, но удалена, восстанавливает ее.
     * Если категория с таким названием существует, но удалена и параметры отличные от указанных, восстанавливает ее.
     * Если категория с таким названием не существует, создает новую с указанными параметрами.
     * @param title название категории
     * @param operationType тип операций (1-расход, 2-доход)
     * @param type тип категории (0-родительская, 1-дочерняя)
     * @param parentId ID родительской категории (null для корневых категорий)
     * @return категория
     */
    public Category get(String title, int operationType, int type, Integer parentId) {
        CommonValidator.validateCategoryTitle(title);
        CommonValidator.validateOperationType(operationType);
        CommonValidator.validateCategoryType(type);
        CommonValidator.validateParentId(parentId);

        Optional<Category> category = categoryRepository.findByTitle(title);
        if (category.isPresent()) {
            Category categoryObj = category.get();
            Category categoryUpd = new Category(categoryObj.getId(), categoryObj.getCreateTime(), categoryObj.getUpdateTime(), categoryObj.getDeleteTime(), categoryObj.getCreatedBy(), categoryObj.getUpdatedBy(), categoryObj.getDeletedBy(), categoryObj.getPosition(), title, operationType, type, parentId);
            
            if (categoryObj.getTitle() == title &&
                categoryObj.getOperationType() == operationType &&
                categoryObj.getType() == type &&
                categoryObj.getParentId() == parentId) {
                
                return categoryObj;
            }

            return update(categoryUpd, title, operationType, type, parentId);
        }

        return create(title, operationType, type, parentId);
    }

    /**
     * Получает категорию по названию или создает новую с указанным типом операций.
     * Если категория с таким названием существует, возвращает ее.
     * Если категория с таким названием существует, но удалена, восстанавливает ее.
     * Если категория с таким названием не существует, создает новую с указанным типом операций.
     * @param title название категории
     * @param operationType тип операций (1-расход, 2-доход)
     * @return категория
     */
    public Category get(String title, int operationType) {
        return get(title, operationType, ModelConstants.DEFAULT_CATEGORY_TYPE, ModelConstants.DEFAULT_PARENT_CATEGORY_ID);
    }

    /**
     * Получает категорию по названию или создает новую с указанным типом операций и типом категории.
     * Если категория с таким названием существует, возвращает ее.
     * Если категория с таким названием существует, но удалена, восстанавливает ее.
     * Если категория с таким названием не существует, создает новую с указанными параметрами.
     * @param title название категории
     * @param operationType тип операций (1-расход, 2-доход)
     * @param type тип категории (0-родительская, 1-дочерняя)
     * @return категория
     */
    public Category get(String title, int operationType, int type) {
        return get(title, operationType, type, ModelConstants.DEFAULT_PARENT_CATEGORY_ID);
    }

    /**
     * Проверяет, удалена ли категория
     * @param category категория для проверки
     * @return true, если категория удалена
     */
    public boolean isCategoryDeleted(Category category) {
        return category.isDeleted();
    }

    /**
     * Восстанавливает удаленную категорию
     * @param restoredCategory категория для восстановления
     * @return восстановленная категория
     */
    private Category restore(Category restoredCategory) {
        restoredCategory.setDeletedBy(null);
        restoredCategory.setDeleteTime(null);
        return update(restoredCategory);
    }    
        
    /**
     * Обновляет категорию
     * @param updatedCategory категория для обновления
     * @param newTitle новое название категории (может быть null)
     * @param newOperationType новое значение типа операции (1-расход, 2-доход) (может быть null)
     * @param newType новое значение типа категории (0-родительская, 1-дочерняя) (может быть null)
     * @param newParentId новое значение ID родительской категории (null для корневых категорий) (может быть null)
     * @return обновленная категория
     */
    public Category update(Category updatedCategory, 
                            String newTitle,
                            Integer newOperationType,
                            Integer newType,
                            Integer newParentId) {
        BaseEntityValidator.validate(updatedCategory);
        
        if (newTitle != null) {
            CommonValidator.validateCategoryTitle(newTitle);
            updatedCategory.setTitle(newTitle);
        }
        
        if (newOperationType != null) {
            CommonValidator.validateOperationType(newOperationType);
            updatedCategory.setOperationType(newOperationType);
        }
        
        if (newType != null) {
            CommonValidator.validateCategoryType(newType);
            updatedCategory.setType(newType);
        }
        
        if (newParentId != null) {
            CommonValidator.validateParentId(newParentId);
            updatedCategory.setParentId(newParentId);
        }

        if (updatedCategory.isDeleted()) {
            return restore(updatedCategory);
        }
        
        // Проверяем, был ли задан хотя бы один параметр для обновления
        if (newTitle != null || newOperationType != null || newType != null || newParentId != null) {
            return update(updatedCategory);
        }
        
        // Если ни один параметр не задан, возвращаем null
        return null;
    }

    /**
     * Обновляет категорию без новых параметров (для внутреннего использования)
     * @param updatedCategory категория для обновления
     * @return обновленная категория
     */
    private Category update(Category updatedCategory) {
        updatedCategory.setUpdateTime(LocalDateTime.now());
        updatedCategory.setUpdatedBy(user);
        
        return categoryRepository.update(updatedCategory);
    }
    
} 