package com.sadengineer.budgetmaster.backend.validator;

import static com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator.*;

import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_TITLE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_TITLE_TOO_SHORT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_TITLE_TOO_LONG;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_TITLE_INVALID_CHARS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_SHORT_NAME_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_SHORT_NAME_TOO_SHORT;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_SHORT_NAME_TOO_LONG;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_SHORT_NAME_INVALID_CHARS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_TITLE_ALREADY_EXISTS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_EXCHANGE_RATE_EMPTY;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.ERROR_CURRENCY_EXCHANGE_RATE_INVALID;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_CURRENCY_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_CURRENCY_TITLE_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_CURRENCY_SHORT_NAME_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MAX_CURRENCY_SHORT_NAME_LENGTH;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.TITLE_PATTERN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.CURRENCY_SHORT_NAME_PATTERN;
import static com.sadengineer.budgetmaster.backend.constants.ValidationConstants.MIN_CURRENCY_EXCHANGE_RATE_VALUE;

import java.util.function.Function;
import java.util.function.BiFunction;

/**
 * Валидатор для валюты
 */
public class CurrencyValidator {

    /**
     * Валидирует название валюты с специфичными правилами
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        validateNotEmpty(title, ERROR_CURRENCY_TITLE_EMPTY);
        String message = String.format(ERROR_CURRENCY_TITLE_TOO_SHORT, MIN_CURRENCY_TITLE_LENGTH);
        validateMinLength(title, MIN_CURRENCY_TITLE_LENGTH, message);
        message = String.format(ERROR_CURRENCY_TITLE_TOO_LONG, MAX_CURRENCY_TITLE_LENGTH);
        validateMaxLength(title, MAX_CURRENCY_TITLE_LENGTH, message);
        validatePattern(title, TITLE_PATTERN, ERROR_CURRENCY_TITLE_INVALID_CHARS);
    }

    /**
     * Валидирует короткое имя валюты с специфичными правилами
     * @param shortName короткое имя для валидации (может быть null)
     * @throws IllegalArgumentException если короткое имя некорректное
     */
    public static void validateShortName(String shortName) {
        validateNotEmpty(shortName, ERROR_CURRENCY_SHORT_NAME_EMPTY);
        String message = String.format(ERROR_CURRENCY_SHORT_NAME_TOO_SHORT, MIN_CURRENCY_SHORT_NAME_LENGTH);
        validateMinLength(shortName, MIN_CURRENCY_SHORT_NAME_LENGTH, message);
        message = String.format(ERROR_CURRENCY_SHORT_NAME_TOO_LONG, MAX_CURRENCY_SHORT_NAME_LENGTH);
        validateMaxLength(shortName, MAX_CURRENCY_SHORT_NAME_LENGTH, message);
        validatePattern(shortName, CURRENCY_SHORT_NAME_PATTERN, ERROR_CURRENCY_SHORT_NAME_INVALID_CHARS);
    }

    /**
     * Валидирует уникальность названия валюты
     * @param title название валюты для проверки
     * @param existsByTitle функция для проверки существования валюты с таким названием
     * @throws IllegalArgumentException если название не уникально
     */
    public static void validateTitleUnique(String title, Function<String, Boolean> existsByTitle) {
        if (title != null && existsByTitle.apply(title)) {
            String message = String.format(ERROR_CURRENCY_TITLE_ALREADY_EXISTS, title);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * Валидирует уникальность короткого имени валюты
     * @param shortName короткое имя валюты для проверки
     * @param existsByShortName функция для проверки существования валюты с таким коротким именем
     * @throws IllegalArgumentException если короткое имя не уникально
     */
    public static void validateShortNameUnique(String shortName, Function<String, Boolean> existsByShortName) {
        if (shortName != null && existsByShortName.apply(shortName)) {
            String message = String.format(ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS, shortName);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * Валидирует уникальность названия валюты (исключая указанную валюту)
     * @param title название валюты для проверки
     * @param excludeId ID валюты для исключения из проверки
     * @param existsByTitleExcludingId функция для проверки существования валюты с таким названием
     * @throws IllegalArgumentException если название не уникально
     */
    public static void validateTitleUniqueExcludingId(String title, int excludeId, BiFunction<String, Integer, Boolean> existsByTitleExcludingId) {
        if (title != null && existsByTitleExcludingId.apply(title, excludeId)) {
            String message = String.format(ERROR_CURRENCY_TITLE_ALREADY_EXISTS, title);
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * Валидирует уникальность короткого имени валюты (исключая указанную валюту)
     * @param shortName короткое имя валюты для проверки
     * @param excludeId ID валюты для исключения из проверки
     * @param existsByShortNameExcludingId функция для проверки существования валюты с таким коротким именем
     * @throws IllegalArgumentException если короткое имя не уникально
     */
    public static void validateShortNameUniqueExcludingId(String shortName, int excludeId, BiFunction<String, Integer, Boolean> existsByShortNameExcludingId) {
        if (shortName != null && existsByShortNameExcludingId.apply(shortName, excludeId)) {
            String message = String.format(ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS, shortName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Валидирует обменный курс валюты
     * @param exchangeRate обменный курс для валидации
     * @throws IllegalArgumentException если обменный курс невалиден
     */
    public static void validateExchangeRate(double exchangeRate) {
        validateNotNull(exchangeRate, ERROR_CURRENCY_EXCHANGE_RATE_EMPTY);
        String message = String.format(ERROR_CURRENCY_EXCHANGE_RATE_INVALID, MIN_CURRENCY_EXCHANGE_RATE_VALUE);
        validateMinValue(exchangeRate, MIN_CURRENCY_EXCHANGE_RATE_VALUE, message);
    }
    
} 