package com.sadengineer.budgetmaster.backend.validator;

import static com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator.*;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_TITLE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_TITLE_TOO_SHORT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_TITLE_TOO_LONG;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_TITLE_INVALID_CHARS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_OPERATION_TYPE_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_TYPE_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_PARENT_ID_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CATEGORY_PARENT_ID_NOT_FOUND;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.TITLE_PATTERN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.OPERATION_TYPE_INCOME;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.OPERATION_TYPE_EXPENSE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.CATEGORY_TYPE_PARENT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.CATEGORY_TYPE_CHILD;

import java.util.Arrays;
import java.util.List;

/**
 * Валидатор для категории
 */
public class CategoryValidator {
    
    /**
     * Валидирует название категории
     * @param title - название категории для валидации
     * @throws IllegalArgumentException если название невалидно
     */
    public static void validateTitle(String title) {
        validateNotEmpty(title, ERROR_CATEGORY_TITLE_EMPTY);
        String message = String.format(ERROR_CATEGORY_TITLE_TOO_SHORT, MIN_TITLE_LENGTH);
        validateMinLength(title, MIN_TITLE_LENGTH, message);
        message = String.format(ERROR_CATEGORY_TITLE_TOO_LONG, MAX_TITLE_LENGTH);
        validateMaxLength(title, MAX_TITLE_LENGTH, message);
        validatePattern(title, TITLE_PATTERN, ERROR_CATEGORY_TITLE_INVALID_CHARS);
    }

    /**
     * Валидирует тип операции
     * @param operationType - тип операции для валидации
     * @throws IllegalArgumentException если тип операции невалиден
     */
    public static void validateOperationType(Integer operationType) {
        List<Integer> listOperationType = Arrays.asList(OPERATION_TYPE_INCOME, OPERATION_TYPE_EXPENSE);
        validateInList(operationType, listOperationType, ERROR_CATEGORY_OPERATION_TYPE_INVALID);
    }

    /**
     * Валидирует тип категории
     * @param type - тип категории для валидации
     * @throws IllegalArgumentException если тип категории невалиден
     */
    public static void validateType(Integer type) {
        List<Integer> listType = Arrays.asList(CATEGORY_TYPE_PARENT, CATEGORY_TYPE_CHILD);
        validateInList(type, listType, ERROR_CATEGORY_TYPE_INVALID);
    }

    /**
     * Валидирует ID родителя категории
     * @param parentID - ID родителя категории для валидации
     * @param maxId - максимальный ID родителя категории
     * @throws IllegalArgumentException если ID родителя категории невалиден
     */
    public static void validateParentId(Integer parentID, int maxId) {
        validateMinValue(parentID, 0, ERROR_CATEGORY_PARENT_ID_INVALID);
        validateMaxValue(parentID, maxId, ERROR_CATEGORY_PARENT_ID_NOT_FOUND);
    }
} 