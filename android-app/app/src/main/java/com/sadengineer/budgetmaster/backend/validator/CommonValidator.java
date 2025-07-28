package com.sadengineer.budgetmaster.backend.validator;

import java.time.LocalDateTime;

/**
 * Общий валидатор для специфичных проверок
 */
public class CommonValidator {
    
    // Константы для типов счетов
    public static final int ACCOUNT_TYPE_CURRENT = 1;
    public static final int ACCOUNT_TYPE_SAVINGS = 2;
    public static final int ACCOUNT_TYPE_CREDIT = 3;
    
    // Константы для типов операций
    public static final int OPERATION_TYPE_EXPENSE = 1;
    public static final int OPERATION_TYPE_INCOME = 2;
    
    // Константы для статусов
    public static final int STATUS_OPEN = 0;
    public static final int STATUS_CLOSED = 1;
    
    /**
     * Валидирует тип счета
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateAccountType(int type) {
        if (type != ACCOUNT_TYPE_CURRENT && 
            type != ACCOUNT_TYPE_SAVINGS && 
            type != ACCOUNT_TYPE_CREDIT) {
            throw new IllegalArgumentException("Некорректный тип счета");
        }
    }
    
    /**
     * Валидирует тип операции
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateOperationType(int type) {
        if (type != OPERATION_TYPE_EXPENSE && 
            type != OPERATION_TYPE_INCOME) {
            throw new IllegalArgumentException("Некорректный тип операции");
        }
    }
    
    /**
     * Валидирует статус закрытия
     * @param status статус для валидации
     * @throws IllegalArgumentException если статус некорректный
     */
    public static void validateClosedStatus(int status) {
        if (status != STATUS_OPEN && 
            status != STATUS_CLOSED) {
            throw new IllegalArgumentException("Некорректный статус закрытия");
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
            BaseEntityValidator.validatePositiveId(parentId.intValue(), "ID родительской категории");
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
     * Валидирует положительную позицию
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePositivePosition(int position) {
        BaseEntityValidator.validatePositivePosition(position, "Позиция");
    }
    
    /**
     * Валидирует сумму операции
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateOperationAmount(int amount) {
        BaseEntityValidator.validateNonNegative(amount, "Сумма операции");
    }
    
    /**
     * Валидирует сумму бюджета
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateBudgetAmount(int amount) {
        BaseEntityValidator.validatePositive(amount, "Сумма бюджета");
    }
    
    /**
     * Валидирует сумму счета
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateAccountAmount(int amount) {
        BaseEntityValidator.validateNonNegative(amount, "Сумма счета");
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
        BaseEntityValidator.validateCurrencyTitle(title, "Название валюты");
    }
    
    /**
     * Валидирует название категории
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateCategoryTitle(String title) {
        BaseEntityValidator.validateTitle(title, "Название категории");
    }
    
    /**
     * Валидирует название счета
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateAccountTitle(String title) {
        BaseEntityValidator.validateTitle(title, "Название счета");
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
        BaseEntityValidator.validateNotEmpty(user, fieldName);
        BaseEntityValidator.validateMaxLength(user, 50, fieldName);
    }
    
    /**
     * Валидирует время создания
     * @param createTime время создания для валидации
     * @throws IllegalArgumentException если время некорректное
     */
    public static void validateCreateTime(LocalDateTime createTime) {
        BaseEntityValidator.validateNotNull(createTime, "Время создания");
        BaseEntityValidator.validateNotFuture(createTime, "Время создания");
    }
    
    /**
     * Валидирует время обновления
     * @param updateTime время обновления для валидации
     * @throws IllegalArgumentException если время некорректное
     */
    public static void validateUpdateTime(LocalDateTime updateTime) {
        BaseEntityValidator.validateNotNull(updateTime, "Время обновления");
        BaseEntityValidator.validateNotFuture(updateTime, "Время обновления");
    }
    
    /**
     * Проверяет, что категория не является родителем самой себя
     * @param categoryId ID категории
     * @param parentId ID родительской категории
     * @throws IllegalArgumentException если категория является родителем самой себя
     */
    public static void validateNotSelfParent(int categoryId, Integer parentId) {
        if (parentId != null && categoryId == parentId) {
            throw new IllegalArgumentException("Категория не может быть родителем самой себя");
        }
    }
} 