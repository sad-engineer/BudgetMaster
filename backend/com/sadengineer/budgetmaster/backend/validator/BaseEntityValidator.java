// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.model.BaseEntity;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import java.time.LocalDateTime;

/**
 * Валидатор для базовой сущности с общими методами валидации
 */
public class BaseEntityValidator {
    
    /**
     * Валидирует базовую сущность
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(BaseEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_NULL_ENTITY);
        }
    }
    
    /**
     * Проверяет, что ID положительный
     * @param id ID для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validatePositiveId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_MUST_BE_POSITIVE);
        }
    }
    
    /**
     * Проверяет, что ID неотрицательный
     * @param id ID для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateNonNegativeId(int id, String fieldName) {
        if (id < 0) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_CANNOT_BE_NEGATIVE);
        }
    }
    
    /**
     * Проверяет, что позиция неотрицательная
     * @param position позиция для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position, String fieldName) {
        if (position < 0) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_CANNOT_BE_NEGATIVE_VALUE);
        }
    }
    
    /**
     * Проверяет, что позиция положительная
     * @param position позиция для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePositivePosition(int position, String fieldName) {
        if (position <= 0) {
            throw new IllegalArgumentException(fieldName + " должна быть больше 0");
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
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_NULL_FIELD);
        }
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_EMPTY_FIELD);
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
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_TOO_LONG + maxLength + " символов");
        }
    }
    
    /**
     * Проверяет, что строка соответствует регулярному выражению
     * @param value строка для проверки
     * @param pattern регулярное выражение
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validatePattern(String value, String pattern, String fieldName) {
        if (value != null && !value.matches(pattern)) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_INVALID_CHARS);
        }
    }
    
    /**
     * Валидирует название (заголовок) с общими правилами
     * @param title название для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title, String fieldName) {
        validateNotEmpty(title, fieldName);
        validateMaxLength(title, ValidationConstants.MAX_TITLE_LENGTH, fieldName);
        validatePattern(title, ValidationConstants.TITLE_PATTERN, fieldName);
    }
    
    /**
     * Валидирует название валюты с специфичными правилами
     * @param title название для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateCurrencyTitle(String title, String fieldName) {
        validateNotEmpty(title, fieldName);
        validateMaxLength(title, ValidationConstants.MAX_CURRENCY_TITLE_LENGTH, fieldName);
        validatePattern(title, ValidationConstants.CURRENCY_TITLE_PATTERN, fieldName);
    }
    
    /**
     * Валидирует комментарий
     * @param comment комментарий для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если комментарий некорректный
     */
    public static void validateComment(String comment, String fieldName) {
        validateMaxLength(comment, ValidationConstants.MAX_COMMENT_LENGTH, fieldName);
        validatePattern(comment, ValidationConstants.COMMENT_PATTERN, fieldName);
    }
    
    /**
     * Проверяет, что число положительное
     * @param value число для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_MUST_BE_POSITIVE_VALUE);
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
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_CANNOT_BE_NEGATIVE_VALUE);
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
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_OUT_OF_RANGE + min + " до " + max);
        }
    }
    
    /**
     * Проверяет, что дата не в будущем
     * @param date дата для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если дата некорректная
     */
    public static void validateNotFuture(LocalDateTime date, String fieldName) {
        if (date != null && date.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_CANNOT_BE_FUTURE);
        }
    }
    
    /**
     * Проверяет, что дата не null
     * @param date дата для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если дата некорректная
     */
    public static void validateNotNull(LocalDateTime date, String fieldName) {
        if (date == null) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_NULL_FIELD);
        }
    }
    
    /**
     * Проверяет, что строка не null
     * @param value строка для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateNotNull(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + ValidationConstants.ERROR_NULL_FIELD);
        }
    }
} 