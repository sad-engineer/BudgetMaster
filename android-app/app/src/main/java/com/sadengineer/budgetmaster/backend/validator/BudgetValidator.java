package com.sadengineer.budgetmaster.backend.validator;

import static com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator.*;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_AMOUNT_MIN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_AMOUNT_MAX;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_CATEGORY_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_CATEGORY_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_CURRENCY_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_CURRENCY_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_BUDGET_AMOUNT_EMPTY;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_AMOUNT_VALUE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_AMOUNT_VALUE;

/**
 * Валидатор для бюджета
 */
public class BudgetValidator {
    
    /**
     * Валидирует сумму бюджета
     * @param amount - сумма для валидации
     * @throws IllegalArgumentException если сумма невалидна
     */
    public static void validateAmount(Long amount) {
        validateNotNull(amount, ERROR_BUDGET_AMOUNT_EMPTY);
        String message = String.format(ERROR_BUDGET_AMOUNT_MIN, MIN_AMOUNT_VALUE);
        validateMinValue(amount, MIN_AMOUNT_VALUE, message);
        message = String.format(ERROR_BUDGET_AMOUNT_MAX, MAX_AMOUNT_VALUE);
        validateMaxValue(amount, MAX_AMOUNT_VALUE, message);
    }

    /**
     * Валидирует ID категории бюджета
     * @param categoryID - ID категории для валидации
     * @throws IllegalArgumentException если ID категории невалиден
     */
    public static void validateCategoryId(Integer categoryID, int maxId) {
        validateMinValue(categoryID, 0, ERROR_BUDGET_CATEGORY_ID_INVALID);
        validateMaxValue(categoryID, maxId, ERROR_BUDGET_CATEGORY_ID_NOT_FOUND);
    }

    /**
     * Валидирует ID валюты бюджета
     * @param currencyID - валюта для валидации
     * @param maxId - максимальный ID валюты
     * @throws IllegalArgumentException если валюта невалидна
     */
    public static void validateCurrencyId(Integer currencyID, int maxId) {
        validateMinValue(currencyID, 0, ERROR_BUDGET_CURRENCY_ID_INVALID);
        validateMaxValue(currencyID, maxId, ERROR_BUDGET_CURRENCY_ID_NOT_FOUND);
    }
} 