// -*- coding: utf-8 -*-
package validator;

import model.Currency;
import validator.BaseEntityValidator;
import java.time.LocalDateTime;

/**
 * Валидатор для валюты
 */
public class CurrencyValidator {

    /**
     * Валидирует id валюты
     * @param id id валюты
     * @throws IllegalArgumentException если id некорректный
     */
    public static void validateId(int id) {
        if (id < 1) {  
            throw new IllegalArgumentException("ID валюты должен быть больше 0");
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
        
        if (title.length() > 80) {
            throw new IllegalArgumentException("Название валюты не может быть длиннее 80 символов");
        }
        
        // Проверка на допустимые символы (буквы, цифры, пробелы, дефисы)
        if (!title.matches("^[а-яА-Яa-zA-Z0-9\\s\\-]+$")) {
            throw new IllegalArgumentException("Название валюты содержит недопустимые символы");
        }
    }

    /**
     * Валидирует позицию валюты
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        if (position < 1) {
            throw new IllegalArgumentException("Позиция валюты должна быть больше 0");
        }
    }

    /**
     * Валидирует пользователя, создавшего валюту
     * @param createdBy пользователь для валидации
     * @throws IllegalArgumentException если пользователь некорректный  
     */
    public static void validateCreatedBy(String createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("Пользователь, создавший валюту, не может быть null");
        }
    }   
    
    /**
     * Валидирует пользователя, обновившего валюту
     * @param updatedBy пользователь для валидации
     * @throws IllegalArgumentException если пользователь некорректный
     */
    public static void validateUpdatedBy(String updatedBy) {    
        if (updatedBy == null) {
            throw new IllegalArgumentException("Пользователь, обновивший валюту, не может быть null");
        }
    }

    /**
     * Валидирует пользователя, удалившего валюту
     * @param deletedBy пользователь для валидации
     * @throws IllegalArgumentException если пользователь некорректный
     */
    public static void validateDeletedBy(String deletedBy) {    
        if (deletedBy == null) {
            throw new IllegalArgumentException("Пользователь, удаливший валюту, не может быть null");
        }
    }
    
    /**
     * Валидирует время создания валюты
     * @param createTime время создания для валидации
     * @throws IllegalArgumentException если время создания некорректное
     */
    public static void validateCreateTime(LocalDateTime createTime) {
        if (createTime == null) {
            throw new IllegalArgumentException("Время создания валюты не может быть null");
        }
    }

    /**
     * Валидирует время обновления валюты
     * @param updateTime время обновления для валидации
     * @throws IllegalArgumentException если время обновления некорректное
     */
    public static void validateUpdateTime(LocalDateTime updateTime) {
        if (updateTime == null) {
            throw new IllegalArgumentException("Время обновления валюты не может быть null");
        }
    }
    
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
        validatePosition(currency.getPosition());
        validateTitle(currency.getTitle());
        validateCreateTime(currency.getCreateTime());
        validateCreatedBy(currency.getCreatedBy());
    }

    /**
     * Валидирует валюту для обновления
     * @param currency валюта для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Currency currency) {
        // Валидация базовых полей
        BaseEntityValidator.validate(currency);

        // Валидация специфичных полей
        validatePosition(currency.getPosition());
        validateTitle(currency.getTitle());
        validateUpdateTime(currency.getUpdateTime());
        validateUpdatedBy(currency.getUpdatedBy());
    }

    

} 