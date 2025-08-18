
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.BiFunction;       

/**
 * Валидатор для валюты
 */
public class CurrencyValidator {
    
    /**
     * Валидирует валюту
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_CURRENCY_NULL);
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);
        
        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        validateTitle(currency.getTitle());
        validateShortName(currency.getShortName());
    }

    /**
     * Валидирует валюту для создания
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_CURRENCY_NULL);
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);

        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        validateTitle(currency.getTitle());
        validateShortName(currency.getShortName());
        CommonValidator.validateCreateTime(currency.getCreateTime());
        CommonValidator.validateUser(currency.getCreatedBy(), ValidationConstants.ERROR_CURRENCY_CREATED_BY_INVALID);
    }

    /**
     * Валидирует валюту для обновления
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_CURRENCY_NULL);
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);

        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        validateTitle(currency.getTitle());
        validateShortName(currency.getShortName());
        CommonValidator.validateUpdateTime(currency.getUpdateTime());
        CommonValidator.validateUser(currency.getUpdatedBy(), ValidationConstants.ERROR_CURRENCY_UPDATED_BY_INVALID);
    }

    /**
     * Валидирует название валюты с специфичными правилами
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        BaseEntityValidator.validateNotEmpty(title, ValidationConstants.ERROR_CURRENCY_TITLE_EMPTY);
        BaseEntityValidator.validateMinLength(title, ValidationConstants.MIN_CURRENCY_TITLE_LENGTH, ValidationConstants.ERROR_CURRENCY_TITLE_TOO_SHORT);
        BaseEntityValidator.validateMaxLength(title, ValidationConstants.MAX_CURRENCY_TITLE_LENGTH, ValidationConstants.ERROR_CURRENCY_TITLE_TOO_LONG);
        BaseEntityValidator.validatePattern(title, ValidationConstants.CURRENCY_TITLE_PATTERN, ValidationConstants.ERROR_CURRENCY_TITLE_INVALID_CHARS);
    }

    /**
     * Валидирует короткое имя валюты с специфичными правилами
     * @param shortName короткое имя для валидации (может быть null)
     * @throws IllegalArgumentException если короткое имя некорректное
     */
    public static void validateShortName(String shortName) {
        BaseEntityValidator.validateNotEmpty(shortName, ValidationConstants.ERROR_CURRENCY_SHORT_NAME_EMPTY);
        BaseEntityValidator.validateMinLength(shortName, ValidationConstants.MIN_CURRENCY_SHORT_NAME_LENGTH, ValidationConstants.ERROR_CURRENCY_SHORT_NAME_TOO_SHORT);
        BaseEntityValidator.validateMaxLength(shortName, ValidationConstants.MAX_CURRENCY_SHORT_NAME_LENGTH, ValidationConstants.ERROR_CURRENCY_SHORT_NAME_TOO_LONG);
        BaseEntityValidator.validatePattern(shortName, ValidationConstants.CURRENCY_SHORT_NAME_PATTERN, ValidationConstants.ERROR_CURRENCY_SHORT_NAME_INVALID_CHARS);
    }

    /**
     * Валидирует ID валюты
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateId(int currencyId) {
        BaseEntityValidator.validatePositiveId(currencyId, ValidationConstants.ERROR_CURRENCY_ID_INVALID);
    }
    
    /**
     * Валидирует уникальность названия валюты
     * @param title название валюты для проверки
     * @param existsByTitle функция для проверки существования валюты с таким названием
     * @throws IllegalArgumentException если название не уникально
     */
    public static void validateTitleUnique(String title, Function<String, Boolean> existsByTitle) {
        if (title != null && existsByTitle.apply(title)) {
            throw new IllegalArgumentException(String.format(ValidationConstants.ERROR_CURRENCY_TITLE_ALREADY_EXISTS, title));
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
            throw new IllegalArgumentException(String.format(ValidationConstants.ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS, shortName));
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
            throw new IllegalArgumentException(String.format(ValidationConstants.ERROR_CURRENCY_TITLE_ALREADY_EXISTS, title));
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
            throw new IllegalArgumentException(String.format(ValidationConstants.ERROR_CURRENCY_SHORT_NAME_ALREADY_EXISTS, shortName));
        }
    }
    
} 