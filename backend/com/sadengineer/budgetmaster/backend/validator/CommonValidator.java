// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import java.time.LocalDateTime;

/**
 * Общий валидатор для специфичных проверок
 */
public class CommonValidator {
    
    /**
     * Валидирует тип счета
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateAccountType(int type) {
        if (type != ValidationConstants.ACCOUNT_TYPE_CURRENT && 
            type != ValidationConstants.ACCOUNT_TYPE_SAVINGS && 
            type != ValidationConstants.ACCOUNT_TYPE_CREDIT) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_ACCOUNT_TYPE);
        }
    }
    
    /**
     * Валидирует тип операции
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateOperationType(int type) {
        if (type != ValidationConstants.OPERATION_TYPE_EXPENSE && 
            type != ValidationConstants.OPERATION_TYPE_INCOME) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_OPERATION_TYPE);
        }
    }
    
    /**
     * Валидирует тип категории
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateCategoryType(int type) {
        if (type != ValidationConstants.CATEGORY_TYPE_PARENT && 
            type != ValidationConstants.CATEGORY_TYPE_CHILD) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_CATEGORY_TYPE);
        }
    }
    
    /**
     * Валидирует статус закрытия
     * @param status статус для валидации
     * @throws IllegalArgumentException если статус некорректный
     */
    public static void validateClosedStatus(int status) {
        if (status != ValidationConstants.STATUS_OPEN && 
            status != ValidationConstants.STATUS_CLOSED) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_CLOSED_STATUS);
        }
    }
    
    /**
     * Валидирует ID валюты
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateId(int currencyId) {
        BaseEntityValidator.validatePositiveId(currencyId, "ID валюты");
    }
    
    /**
     * Валидирует ID валюты (может быть null)
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCurrencyId(Integer currencyId) {
        if (currencyId != null) {
            validateId(currencyId.intValue());
        }
    }
    
    /**
     * Валидирует ID категории
     * @param categoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCategoryId(int categoryId) {
        BaseEntityValidator.validatePositiveId(categoryId, "ID категории");
    }
    
    /**
     * Валидирует ID категории (может быть null)
     * @param categoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCategoryId(Integer categoryId) {
        if (categoryId != null) {
            BaseEntityValidator.validatePositiveId(categoryId.intValue(), "ID категории");
        }
    }
    
    /**
     * Валидирует ID счета
     * @param accountId ID счета для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateAccountId(int accountId) {
        BaseEntityValidator.validatePositiveId(accountId, "ID счета");
    }
    
    /**
     * Валидирует ID счета (может быть null)
     * @param accountId ID счета для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateAccountId(Integer accountId) {
        if (accountId != null) {
            BaseEntityValidator.validatePositiveId(accountId.intValue(), "ID счета");
        }
    }
    
    /**
     * Валидирует ID родительской категории
     * @param parentId ID родительской категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateParentId(Integer parentId) {
        if (parentId != null) {
            validateCategoryId(parentId);
        }
    }
    
    /**
     * Валидирует позицию
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        BaseEntityValidator.validatePosition(position, "Позиция");
    }
    
    /**
     * Валидирует позицию (положительная)
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePositivePosition(int position) {
        BaseEntityValidator.validatePositivePosition(position, "Позиция");
    }
    
    /**
     * Валидирует сумму операции (положительная)
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateOperationAmount(int amount) {
        BaseEntityValidator.validatePositive(amount, "Сумма операции");
    }
    
    /**
     * Валидирует сумму бюджета (неотрицательная)
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateBudgetAmount(int amount) {
        BaseEntityValidator.validateNonNegative(amount, "Сумма бюджета");
    }
    
    /**
     * Валидирует сумму счета (может быть отрицательной для кредита)
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateAccountAmount(int amount) {
        // Сумма счета может быть отрицательной (кредит)
        // Никаких проверок не требуется
    }
    
    /**
     * Валидирует дату операции
     * @param date дата для валидации
     * @throws IllegalArgumentException если дата некорректная
     */
    public static void validateOperationDate(LocalDateTime date) {
        BaseEntityValidator.validateNotNull(date, "Дата операции");
        BaseEntityValidator.validateNotFuture(date, "Дата операции");
    }
    
    /**
     * Валидирует название
     * @param title название для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title, String fieldName) {
        BaseEntityValidator.validateTitle(title, fieldName);
    }
    
    /**
     * Валидирует название валюты
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateCurrencyTitle(String title) {
        validateTitle(title, "Название валюты");
    }

    /**
     * Валидирует название категории
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateCategoryTitle(String title) {
        validateTitle(title, "Название категории");
    }

    /**
     * Валидирует название счета
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateAccountTitle(String title) {
        validateTitle(title, "Название счета");
    }
    
    /**
     * Валидирует комментарий
     * @param comment комментарий для валидации
     * @throws IllegalArgumentException если комментарий некорректный
     */
    public static void validateComment(String comment) {
        BaseEntityValidator.validateComment(comment, "Комментарий");
    }
    
    /**
     * Валидирует пользователя
     * @param user пользователь для валидации
     * @param fieldName название поля для сообщения об ошибке
     * @throws IllegalArgumentException если пользователь некорректный
     */
    public static void validateUser(String user, String fieldName) {
        BaseEntityValidator.validateNotNull(user, fieldName);
    }
    
    /**
     * Валидирует время создания
     * @param createTime время создания для валидации
     * @throws IllegalArgumentException если время некорректное
     */
    public static void validateCreateTime(LocalDateTime createTime) {
        BaseEntityValidator.validateNotNull(createTime, "Время создания");
    }
    
    /**
     * Валидирует время обновления
     * @param updateTime время обновления для валидации
     * @throws IllegalArgumentException если время некорректное
     */
    public static void validateUpdateTime(LocalDateTime updateTime) {
        BaseEntityValidator.validateNotNull(updateTime, "Время обновления");
    }
    
    /**
     * Проверяет, что категория не является родителем самой себя
     * @param categoryId ID категории
     * @param parentId ID родительской категории
     * @throws IllegalArgumentException если категория является родителем самой себя
     */
    public static void validateNotSelfParent(int categoryId, Integer parentId) {
        if (parentId != null && categoryId == parentId) {
            throw new IllegalArgumentException(ValidationConstants.ERROR_SELF_PARENT);
        }
    }
} 