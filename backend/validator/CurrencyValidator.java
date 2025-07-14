// -*- coding: utf-8 -*-
package validator;

import model.Currency;
import validator.BaseEntityValidator;
import validator.CommonValidator;
import java.time.LocalDateTime;

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
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);
        
        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        CommonValidator.validateCurrencyTitle(currency.getTitle());
    }

    /**
     * Валидирует валюту для создания
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);

        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        CommonValidator.validateCurrencyTitle(currency.getTitle());
        CommonValidator.validateCreateTime(currency.getCreateTime());
        CommonValidator.validateUser(currency.getCreatedBy(), "Пользователь, создавший валюту");
    }

    /**
     * Валидирует валюту для обновления
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);

        // Валидация специфичных полей
        CommonValidator.validatePositivePosition(currency.getPosition());
        CommonValidator.validateCurrencyTitle(currency.getTitle());
        CommonValidator.validateUpdateTime(currency.getUpdateTime());
        CommonValidator.validateUser(currency.getUpdatedBy(), "Пользователь, обновивший валюту");
    }
} 