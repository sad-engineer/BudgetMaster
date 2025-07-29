// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;

/**
 * Валидатор для категории
 */
public class CategoryValidator {
    
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
} 