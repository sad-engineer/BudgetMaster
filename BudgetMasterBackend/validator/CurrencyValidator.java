// -*- coding: utf-8 -*-
package validator;

import model.Currency;
import validator.BaseEntityValidator;

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
        validatePosition(currency.getPosition());
        validateTitle(currency.getTitle());
    }
    
    /**
     * Валидирует позицию валюты
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Позиция валюты не может быть отрицательной");
        }
    }
    
    /**
     * Валидирует название валюты
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Название валюты не может быть null");
        }
        
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название валюты не может быть пустым");
        }
        
        if (title.length() > 100) {
            throw new IllegalArgumentException("Название валюты не может быть длиннее 100 символов");
        }
        
        // Проверка на допустимые символы (буквы, цифры, пробелы, дефисы)
        if (!title.matches("^[а-яА-Яa-zA-Z0-9\\s\\-]+$")) {
            throw new IllegalArgumentException("Название валюты содержит недопустимые символы");
        }
    }
    
    /**
     * Валидирует валюту для создания (без ID)
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Валюта не может быть null");
        }
        
        validatePosition(currency.getPosition());
        validateTitle(currency.getTitle());
    }
    
    /**
     * Валидирует валюту для обновления
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Currency currency) {
        validate(currency);
    }
} 