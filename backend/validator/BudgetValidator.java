// -*- coding: utf-8 -*-
package validator;

import model.Budget;
import validator.BaseEntityValidator;
import validator.CommonValidator;

/**
 * Валидатор для бюджета
 */
public class BudgetValidator {
    
    /**
     * Валидирует бюджет
     * @param budget бюджет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Бюджет не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(budget);

        // Валидация специфичных полей
        CommonValidator.validateBudgetAmount(budget.getAmount());
        CommonValidator.validateCurrencyId(budget.getCurrencyId());
        CommonValidator.validateCategoryId(budget.getCategoryId());
        CommonValidator.validatePosition(budget.getPosition());
    }
    
    /**
     * Валидирует бюджет для создания (без ID)
     * @param budget бюджет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Бюджет не может быть null");
        }
        
        CommonValidator.validateBudgetAmount(budget.getAmount());
        CommonValidator.validateCurrencyId(budget.getCurrencyId());
        CommonValidator.validateCategoryId(budget.getCategoryId());
        CommonValidator.validatePosition(budget.getPosition());
    }
    
    /**
     * Валидирует бюджет для обновления
     * @param budget бюджет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Бюджет не может быть null");
        }
        
        CommonValidator.validateBudgetAmount(budget.getAmount());
        CommonValidator.validateCurrencyId(budget.getCurrencyId());
        CommonValidator.validateCategoryId(budget.getCategoryId());
        CommonValidator.validatePosition(budget.getPosition());
    }
} 