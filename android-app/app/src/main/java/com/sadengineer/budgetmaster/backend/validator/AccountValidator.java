package com.sadengineer.budgetmaster.backend.validator;

import static com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator.*;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_TITLE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_TITLE_TOO_SHORT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_TITLE_TOO_LONG;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_TITLE_INVALID_CHARS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_BALANCE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_BALANCE_MIN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_BALANCE_MAX;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_ACCOUNT_TYPE_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_ID_MIN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_ID_MAX;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.STATUS_OPEN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.STATUS_CLOSED;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CLOSED_STATUS;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.TITLE_PATTERN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_AMOUNT_VALUE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_AMOUNT_VALUE;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ACCOUNT_TYPE_CURRENT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ACCOUNT_TYPE_SAVINGS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ACCOUNT_TYPE_CREDIT;

import java.util.Arrays;
import java.util.List;

/**
 * Валидатор для счетов
 */
public class AccountValidator {
    
    /**
     * Валидирует название счета
     * @param title - название счета для валидации
     * @throws IllegalArgumentException если название невалидно
     */
    public static void validateTitle(String title) {
        validateNotEmpty(title, ERROR_ACCOUNT_TITLE_EMPTY);
        String message = String.format(ERROR_ACCOUNT_TITLE_TOO_SHORT, MIN_TITLE_LENGTH);
        validateMinLength(title, MIN_TITLE_LENGTH, message);
        message = String.format(ERROR_ACCOUNT_TITLE_TOO_LONG, MAX_TITLE_LENGTH);
        validateMaxLength(title, MAX_TITLE_LENGTH, message);
        validatePattern(title, TITLE_PATTERN, ERROR_ACCOUNT_TITLE_INVALID_CHARS);
    }
    
    /**
     * Валидирует сумму баланса счета
     * @param amount - сумма баланс для валидации
     * @throws IllegalArgumentException если баланс невалиден
     */
    public static void validateAmount(long amount) {
        validateNotNull(amount, ERROR_ACCOUNT_BALANCE_EMPTY);
        String message = String.format(ERROR_ACCOUNT_BALANCE_MIN, MIN_AMOUNT_VALUE);
        validateMinValue(amount, MIN_AMOUNT_VALUE, message);
        message = String.format(ERROR_ACCOUNT_BALANCE_MAX, MAX_AMOUNT_VALUE);
        validateMaxValue(amount, MAX_AMOUNT_VALUE, message);
    }
    
    /**
     * Валидирует ID валюты счета
     * @param currencyID - валюта для валидации
     * @throws IllegalArgumentException если валюта невалидна
     */
    public static void validateCurrencyId(Integer currencyID, int maxId) {
        String message = String.format(ERROR_CURRENCY_ID_MIN, 0);
        validateMinValue(currencyID, 0, message);
        message = String.format(ERROR_CURRENCY_ID_MAX, maxId);
        validateMaxValue(currencyID, maxId, message);
    }

    /**
     * Валидирует тип счета
     * @param type - тип счета для валидации
     * @throws IllegalArgumentException если тип невалиден
     */
    public static void validateType(Integer type) {
        List<Integer> listType = Arrays.asList(ACCOUNT_TYPE_CURRENT, ACCOUNT_TYPE_SAVINGS, ACCOUNT_TYPE_CREDIT);
        validateInList(type, listType, ERROR_ACCOUNT_TYPE_INVALID);
    }   

    /**
     * Валидирует статус закрытия счета
     * @param closed - статус закрытия для валидации
     * @throws IllegalArgumentException если статус невалиден
     */
    public static void validateClosed(Integer closed) {
        List<Integer> listClosed = Arrays.asList(STATUS_OPEN, STATUS_CLOSED);
        validateInList(closed, listClosed, ERROR_CLOSED_STATUS);
    }
} 

