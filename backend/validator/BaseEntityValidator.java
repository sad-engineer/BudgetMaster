// -*- coding: utf-8 -*-
package validator;

import model.BaseEntity;

/**
 * Валидатор для базовой сущности
 */
public class BaseEntityValidator {
    
    /**
     * Валидирует базовую сущность
     * @param entity сущность для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(BaseEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Сущность не может быть null");
        }
    }
} 