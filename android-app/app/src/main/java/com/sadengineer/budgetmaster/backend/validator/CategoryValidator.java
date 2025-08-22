
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;

/**
 * Валидатор для категории
 */
public class CategoryValidator {
    
    // Минимальная длина названия категории
    private static final int MIN_TITLE_LENGTH = 1;
    
    // Максимальная длина названия категории
    private static final int MAX_TITLE_LENGTH = 200;
    
    /**
     * Валидирует категорию
     * @param category категория для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(category);
        
        // Валидация специфичных полей
        CommonValidator.validatePosition(category.getPosition());
        CommonValidator.validateTitle(category.getTitle(), "Название категории");
        CommonValidator.validateCategoryType(category.getOperationType());
        CommonValidator.validateParentId(category.getParentId());
        CommonValidator.validateNotSelfParent(category.getId(), category.getParentId());
    }
    
    /**
     * Валидирует категорию для создания (без ID)
     * @param category категория для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        CommonValidator.validatePosition(category.getPosition());
        CommonValidator.validateTitle(category.getTitle(), "Название категории");
        CommonValidator.validateCategoryType(category.getOperationType());
        CommonValidator.validateParentId(category.getParentId());
        CommonValidator.validateNotSelfParent(category.getId(), category.getParentId());
    }
    
    /**
     * Валидирует категорию для обновления
     * @param category категория для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        CommonValidator.validatePosition(category.getPosition());
        CommonValidator.validateTitle(category.getTitle(), "Название категории");
        CommonValidator.validateCategoryType(category.getOperationType());
        CommonValidator.validateParentId(category.getParentId());
        CommonValidator.validateNotSelfParent(category.getId(), category.getParentId());
    }

    /**
     * Валидирует название категории
     * @param title - название категории для валидации
     * @throws IllegalArgumentException если название невалидно
     */
    public static void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        
        String trimmedTitle = title.trim();
        
        if (trimmedTitle.isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        
        if (trimmedTitle.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException("Название категории должно содержать минимум " + MIN_TITLE_LENGTH + " символ");
        }
        
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Название категории не может быть длиннее " + MAX_TITLE_LENGTH + " символов");
        }
        
        // Проверяем на специальные символы
        if (!trimmedTitle.matches("^[a-zA-Zа-яА-Я0-9\\s\\-_\\.]+$")) {
            throw new IllegalArgumentException("Название категории содержит недопустимые символы");
        }
    }
} 