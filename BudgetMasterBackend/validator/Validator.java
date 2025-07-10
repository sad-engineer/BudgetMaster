// -*- coding: utf-8 -*-
package validator;

import model.*;

/**
 * Общий валидатор для всех моделей
 */
public class Validator {
    
    /**
     * Валидирует любую сущность по её типу
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
        
        if (entity instanceof Currency) {
            CurrencyValidator.validate((Currency) entity);
        } else if (entity instanceof Category) {
            CategoryValidator.validate((Category) entity);
        } else if (entity instanceof Account) {
            AccountValidator.validate((Account) entity);
        } else if (entity instanceof Budget) {
            BudgetValidator.validate((Budget) entity);
        } else if (entity instanceof Operation) {
            OperationValidator.validate((Operation) entity);
        } else {
            throw new IllegalArgumentException("Неизвестный тип сущности: " + entity.getClass().getSimpleName());
        }
    }
    
    /**
     * Валидирует сущность для создания (без ID)
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
        
        if (entity instanceof Currency) {
            CurrencyValidator.validateForCreate((Currency) entity);
        } else if (entity instanceof Category) {
            CategoryValidator.validateForCreate((Category) entity);
        } else if (entity instanceof Account) {
            AccountValidator.validateForCreate((Account) entity);
        } else if (entity instanceof Budget) {
            BudgetValidator.validateForCreate((Budget) entity);
        } else if (entity instanceof Operation) {
            OperationValidator.validateForCreate((Operation) entity);
        } else {
            throw new IllegalArgumentException("Неизвестный тип сущности: " + entity.getClass().getSimpleName());
        }
    }
    
    /**
     * Валидирует сущность для обновления
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
        
        if (entity instanceof Currency) {
            CurrencyValidator.validateForUpdate((Currency) entity);
        } else if (entity instanceof Category) {
            CategoryValidator.validateForUpdate((Category) entity);
        } else if (entity instanceof Account) {
            AccountValidator.validateForUpdate((Account) entity);
        } else if (entity instanceof Budget) {
            BudgetValidator.validateForUpdate((Budget) entity);
        } else if (entity instanceof Operation) {
            OperationValidator.validateForUpdate((Operation) entity);
        } else {
            throw new IllegalArgumentException("Неизвестный тип сущности: " + entity.getClass().getSimpleName());
        }
    }
    
    /**
     * Проверяет, что ID положительный
     * @param id ID для проверки
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным");
        }
    }
    
    /**
     * Проверяет, что ID неотрицательный
     * @param id ID для проверки
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateNonNegativeId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID не может быть отрицательным");
        }
    }
    
    /**
     * Проверяет, что строка не пустая
     * @param value строка для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
    
    /**
     * Проверяет, что строка не превышает максимальную длину
     * @param value строка для проверки
     * @param maxLength максимальная длина
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " не может быть длиннее " + maxLength + " символов");
        }
    }
    
    /**
     * Проверяет, что число положительное
     * @param value число для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " должно быть положительным");
        }
    }
    
    /**
     * Проверяет, что число неотрицательное
     * @param value число для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validateNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " не может быть отрицательным");
        }
    }
    
    /**
     * Проверяет, что число находится в допустимом диапазоне
     * @param value число для проверки
     * @param min минимальное значение
     * @param max максимальное значение
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " должно быть в диапазоне от " + min + " до " + max);
        }
    }
} 