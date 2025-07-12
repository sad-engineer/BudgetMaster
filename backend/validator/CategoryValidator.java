// -*- coding: utf-8 -*-
package validator;

import model.Category;
import validator.BaseEntityValidator;

/**
 * Валидатор для категории
 */
public class CategoryValidator {
    
    /**
     * Валидирует категорию
     * @param category категория для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(category);
        
        // Валидация специфичных полей
        validatePosition(category.getPosition());
        validateTitle(category.getTitle());
        validateOperationType(category.getOperationType());
        validateType(category.getType());
        validateParentId(category.getParentId());
        validateNotSelfParent(category);
    }
    
    /**
     * Валидирует позицию категории
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Позиция категории не может быть отрицательной");
        }
    }
    
    /**
     * Валидирует название категории
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Название категории не может быть null");
        }
        
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Название категории не может быть длиннее 200 символов");
        }
        
        // Проверка на допустимые символы (буквы, цифры, пробелы, дефисы, скобки)
        if (!title.matches("^[а-яА-Яa-zA-Z0-9\\s\\-\\(\\)]+$")) {
            throw new IllegalArgumentException("Название категории содержит недопустимые символы");
        }
    }
    
    /**
     * Валидирует тип операции категории
     * @param operationType тип операции для валидации
     * @throws IllegalArgumentException если тип операции некорректный
     */
    public static void validateOperationType(int operationType) {
        if (operationType != 1 && operationType != 2) {
            throw new IllegalArgumentException("Тип операции должен быть 1 (расход) или 2 (доход)");
        }
    }
    
    /**
     * Валидирует тип категории
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateType(int type) {
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("Тип категории должен быть 1 (обычная) или 2 (системная)");
        }
    }
    
    /**
     * Валидирует ID родительской категории
     * @param parentId ID родительской категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateParentId(Integer parentId) {
        if (parentId != null && parentId <= 0) {
            throw new IllegalArgumentException("ID родительской категории должен быть положительным");
        }
    }
    
    /**
     * Валидирует категорию для создания (без ID)
     * @param category категория для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        
        validatePosition(category.getPosition());
        validateTitle(category.getTitle());
        validateOperationType(category.getOperationType());
        validateType(category.getType());
        validateParentId(category.getParentId());

        validateNotSelfParent(category);
    }
    
    /**
     * Проверяет, что категория не является родителем самой себя
     * @param category категория для проверки
     * @throws IllegalArgumentException если категория является родителем самой себя
     */
    public static void validateNotSelfParent(Category category) {
        if (category.getParentId() != null && category.getId() == category.getParentId()) {
            throw new IllegalArgumentException("Категория не может быть родителем самой себя");
        }
    }
} 