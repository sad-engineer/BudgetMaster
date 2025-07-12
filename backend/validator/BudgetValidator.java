// -*- coding: utf-8 -*-
package validator;

import model.Budget;
import validator.BaseEntityValidator;

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
        validateAmount(budget.getAmount());
        validateCurrencyId(budget.getCurrencyId());
        validateCategoryId(budget.getCategoryId());
        validatePosition(budget.getPosition());
    }
    
    /**
     * Валидирует сумму бюджета
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Сумма бюджета не может быть отрицательной");
        }
    }
    
    /**
     * Валидирует ID валюты
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCurrencyId(int currencyId) {
        if (currencyId <= 0) {
            throw new IllegalArgumentException("ID валюты должен быть положительным");
        }
    }
    
    /**
     * Валидирует ID категории
     * @param categoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCategoryId(Integer categoryId) {
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException("ID категории должен быть положительным");
        }
    }
    
    /**
     * Валидирует позицию бюджета
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Позиция бюджета не может быть отрицательной");
        }
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
        
        validateAmount(budget.getAmount());
        validateCurrencyId(budget.getCurrencyId());
        validateCategoryId(budget.getCategoryId());
        validatePosition(budget.getPosition());
    }
} 