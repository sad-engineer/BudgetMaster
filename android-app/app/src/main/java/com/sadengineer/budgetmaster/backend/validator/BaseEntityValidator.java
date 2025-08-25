package com.sadengineer.budgetmaster.backend.validator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Валидатор для базовой сущности с общими методами валидации
 */
public class BaseEntityValidator {
    
    /**
     * Валидирует базовую сущность
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Object entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
    }
    
    /**
     * Проверяет, что ID положительный
     * @param id ID для проверки
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validatePositiveId(int id, String errorMessage) {
        if (id <= 0) {
            throw new IllegalArgumentException(errorMessage);
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
            throw new IllegalArgumentException(fieldName + " не может быть отрицательным");
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
            throw new IllegalArgumentException(fieldName + " не может быть отрицательным");
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
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateNotEmpty(String value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * Проверяет, что строка не превышает максимальную длину
     * @param value строка для проверки
     * @param maxLength максимальная длина
     * @param errorMessage сообщение об ошибке (может содержать %d для подстановки maxLength)
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateMaxLength(String value, int maxLength, String errorMessage) {
        if (value != null && value.length() > maxLength) {
            if (errorMessage.contains("%d")) {
                throw new IllegalArgumentException(String.format(errorMessage, maxLength));
            } else {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Проверяет, что строка превышает минимальную длину
     * @param value строка для проверки
     * @param minLength минимальная длина
     * @param errorMessage сообщение об ошибке (может содержать %d для подстановки minLength)
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validateMinLength(String value, int minLength, String errorMessage) {
        if (value != null && value.length() < minLength) {
            if (errorMessage.contains("%d")) {
                throw new IllegalArgumentException(String.format(errorMessage, minLength));
            } else {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
    
    /**
     * Проверяет, что строка соответствует регулярному выражению
     * @param value строка для проверки
     * @param pattern регулярное выражение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если строка некорректная
     */
    public static void validatePattern(String value, String pattern, String errorMessage) {
        if (value != null && !value.matches(pattern)) {
            throw new IllegalArgumentException(errorMessage);
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
        validateMaxLength(title, 100, fieldName);
        validatePattern(title, "^[a-zA-Zа-яА-Я0-9\\s\\-_.,!?()]+$", fieldName);
    }
    
    
    /**
     * Валидирует комментарий
     * @param comment комментарий для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если комментарий некорректный
     */
    public static void validateComment(String comment, String fieldName) {
        validateMaxLength(comment, 500, fieldName);
        validatePattern(comment, "^[a-zA-Zа-яА-Я0-9\\s\\-_.,!?()]+$", fieldName);
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
    
    /**
     * Проверяет, что дата не в будущем
     * @param date дата для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если дата некорректная
     */
    public static void validateNotFuture(LocalDateTime date, String fieldName) {
        if (date != null && date.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(fieldName + " не может быть в будущем");
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
            throw new IllegalArgumentException(fieldName + " не может быть null");
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
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
    
    /**
     * Проверяет, что число не null
     * @param value число для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validateNotNull(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
    
    /**
     * Проверяет, что число не null
     * @param value число для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если число некорректное
     */
    public static void validateNotNull(Double value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }

    /**
     * Проверяет, что значение не null
     * @param value значение для проверки
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если значение null
     */
    public static void validateNotNull(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(int value, int minValue, String errorMessage) {
        if (value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(Integer value, int minValue, String errorMessage) {
        if (value != null && value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(double value, double minValue, String errorMessage) {
        if (value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(Double value, double minValue, String errorMessage) {
        if (value != null && value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(long value, long minValue, String errorMessage) {
        if (value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не меньше минимального
     * @param value значение для проверки
     * @param minValue минимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение меньше минимального
     */
    public static void validateMinValue(Long value, long minValue, String errorMessage) {
        if (value != null && value < minValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(int value, int maxValue, String errorMessage) {
        if (value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(Integer value, int maxValue, String errorMessage) {
        if (value != null && value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(double value, double maxValue, String errorMessage) {
        if (value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(Double value, double maxValue, String errorMessage) {
        if (value != null && value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(long value, long maxValue, String errorMessage) {
        if (value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Проверяет, что значение не больше максимального
     * @param value значение для проверки
     * @param maxValue максимальное значение
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение больше максимального
     */
    public static void validateMaxValue(Long value, long maxValue, String errorMessage) {
        if (value != null && value > maxValue) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * Проверяет, что значение находится в списке допустимых значений
     * @param value значение для проверки
     * @param allowedValues список допустимых значений
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение некорректное
     */
    public static void validateInList(Integer value, List<Integer> allowedValues, String errorMessage) {
        if (value != null && !allowedValues.contains(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    /**
     * Проверяет, что значение находится в списке допустимых значений
     * @param value значение для проверки
     * @param allowedValues список допустимых значений
     * @param errorMessage сообщение об ошибке
     * @throws IllegalArgumentException если значение некорректное
     */
    public static void validateInList(String value, List<String> allowedValues, String errorMessage) {
        if (value != null && !allowedValues.contains(value)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
} 