package com.sadengineer.budgetmaster.backend.validator;

import static com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator.*;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_TYPE_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_DATE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_AMOUNT_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_COMMENT_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_COMMENT_TOO_SHORT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_COMMENT_TOO_LONG;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_COMMENT_INVALID_CHARS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_CATEGORY_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_CATEGORY_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_ACCOUNT_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_ACCOUNT_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_CURRENCY_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_CURRENCY_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_OPERATION_AMOUNT_EMPTY;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.OPERATION_TYPE_INCOME;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.OPERATION_TYPE_EXPENSE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_AMOUNT_VALUE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_AMOUNT_VALUE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_COMMENT_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_COMMENT_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.COMMENT_PATTERN;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Валидатор для операции
 */
public class OperationValidator {
    
    /**
     * Валидирует тип операции
     * @param type - тип операции для валидации
     * @throws IllegalArgumentException если тип операции невалиден
     */
    public static void validateType(Integer type) {
        validateNotNull(type, ERROR_OPERATION_TYPE_INVALID);
        List<Integer> listType = Arrays.asList(OPERATION_TYPE_INCOME, OPERATION_TYPE_EXPENSE);
        validateInList(type, listType, ERROR_OPERATION_TYPE_INVALID);
    }

    /**
     * Валидирует дату операции
     * @param date - дата операции для валидации
     * @throws IllegalArgumentException если дата невалидна
     */
    public static void validateDate(LocalDateTime date) {
        validateNotNull(date, ERROR_OPERATION_DATE_EMPTY);
    }

    /**
     * Валидирует сумму операции
     * @param amount - сумма операции для валидации
     * @throws IllegalArgumentException если сумма невалидна
     */
    public static void validateAmount(Long amount) {
        validateNotNull(amount, ERROR_OPERATION_AMOUNT_EMPTY);
        validateMinValue(amount, MIN_AMOUNT_VALUE, ERROR_OPERATION_AMOUNT_INVALID);
        validateMaxValue(amount, MAX_AMOUNT_VALUE, ERROR_OPERATION_AMOUNT_INVALID);
    }

    /**
     * Валидирует комментарий операции
     * @param comment - комментарий операции для валидации
     * @throws IllegalArgumentException если комментарий невалиден
     */
    public static void validateComment(String comment) {
        validateNotEmpty(comment, ERROR_OPERATION_COMMENT_EMPTY);
        String message = String.format(ERROR_OPERATION_COMMENT_TOO_SHORT, MIN_COMMENT_LENGTH);
        validateMinLength(comment, MIN_COMMENT_LENGTH, message);
        message = String.format(ERROR_OPERATION_COMMENT_TOO_LONG, MAX_COMMENT_LENGTH);
        validateMaxLength(comment, MAX_COMMENT_LENGTH, message);
        validatePattern(comment, COMMENT_PATTERN, ERROR_OPERATION_COMMENT_INVALID_CHARS);
    }

    /**
     * Валидирует ID категории операции
     * @param categoryId - ID категории для валидации
     * @param maxId - максимальный ID категории
     * @throws IllegalArgumentException если ID категории невалиден
     */
    public static void validateCategoryId(Integer categoryId, int maxId) {
        validateNotNull(categoryId, ERROR_OPERATION_CATEGORY_ID_INVALID);
        validateMinValue(categoryId, 0, ERROR_OPERATION_CATEGORY_ID_INVALID);
        validateMaxValue(categoryId, maxId, ERROR_OPERATION_CATEGORY_ID_NOT_FOUND);
    }

    /**
     * Валидирует ID счета операции
     * @param accountId - ID счета для валидации
     * @param maxId - максимальный ID счета
     * @throws IllegalArgumentException если ID счета невалиден
     */
    public static void validateAccountId(Integer accountId, int maxId) {
        validateNotNull(accountId, ERROR_OPERATION_ACCOUNT_ID_INVALID);
        validateMinValue(accountId, 0, ERROR_OPERATION_ACCOUNT_ID_INVALID);
        validateMaxValue(accountId, maxId, ERROR_OPERATION_ACCOUNT_ID_NOT_FOUND);
    }

    /**
     * Валидирует ID валюты операции
     * @param currencyId - ID валюты для валидации
     * @param maxId - максимальный ID валюты
     * @throws IllegalArgumentException если ID валюты невалиден
     */
    public static void validateCurrencyId(Integer currencyId, int maxId) {
        validateNotNull(currencyId, ERROR_OPERATION_CURRENCY_ID_INVALID);
        validateMinValue(currencyId, 0, ERROR_OPERATION_CURRENCY_ID_INVALID);
        validateMaxValue(currencyId, maxId, ERROR_OPERATION_CURRENCY_ID_NOT_FOUND);
    }

    // TODO: validateToAccountId, validateToCurrencyId, validateToAmount 
} 