// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
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
        validateTitle(currency.getTitle());
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
        validateTitle(currency.getTitle());
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
        validateTitle(currency.getTitle());
        CommonValidator.validateUpdateTime(currency.getUpdateTime());
        CommonValidator.validateUser(currency.getUpdatedBy(), "Пользователь, обновивший валюту");
    }

    /**
     * Валидирует название валюты с специфичными правилами
     * @param title название для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        BaseEntityValidator.validateNotEmpty(title, "Название валюты");
        BaseEntityValidator.validateMinLength(title, 3, "Название валюты");
        BaseEntityValidator.validateMaxLength(title, 70, "Название валюты");
        BaseEntityValidator.validatePattern(title, "^[a-zA-Zа-яА-Я\\s\\-]+$", "Название валюты");
    }

    /**
     * Валидирует ID валюты
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateId(int currencyId) {
        BaseEntityValidator.validatePositiveId(currencyId, "ID валюты");
    }
    
} 