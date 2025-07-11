// -*- coding: utf-8 -*-
package validator;

import model.Account;
import validator.BaseEntityValidator;

/**
 * Валидатор для счета
 */
public class AccountValidator {
    
    /**
     * Валидирует счет
     * @param account счет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Счет не может быть null");
        }
        
        // Валидация базовых полей
        BaseEntityValidator.validate(account);
        
        // Валидация специфичных полей
        validateTitle(account.getTitle());
        validatePosition(account.getPosition());
        validateAmount(account.getAmount());
        validateType(account.getType());
        validateCurrencyId(account.getCurrencyId());
        validateClosed(account.getClosed());
        // TODO: Сделать валидацию полей для кредитной карты после реализации логики 
        //validateCreditCardLimit(account.getCreditCardLimit());
        //validateCreditCardCategoryId(account.getCreditCardCategoryId());
        //validateCreditCardCommissionCategoryId(account.getCreditCardCommissionCategoryId());
    }
    
    /**
     * Валидирует позицию счета
     * @param position позиция для валидации
     * @throws IllegalArgumentException если позиция некорректная
     */
    public static void validatePosition(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Позиция счета не может быть отрицательной");
        }
    }
    
    /**
     * Валидирует название счета
     * @param title название для валидации
     * @throws IllegalArgumentException если название некорректное
     */
    public static void validateTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Название счета не может быть null");
        }
        
        if (title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название счета не может быть пустым");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Название счета не может быть длиннее 200 символов");
        }
        
        // Проверка на допустимые символы (буквы, цифры, пробелы, дефисы, скобки)
        if (!title.matches("^[а-яА-Яa-zA-Z0-9\\s\\-\\(\\)]+$")) {
            throw new IllegalArgumentException("Название счета содержит недопустимые символы");
        }
    }
    
    /**
     * Валидирует сумму счета
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateAmount(int amount) {
        // Сумма может быть отрицательной (кредит)
    }
    
    /**
     * Валидирует тип счета
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateType(int type) {
        if (type != 1 && type != 2 && type != 3) {
            throw new IllegalArgumentException("Тип счета должен быть 1 (расчетный), 2 (сберегательный) или 3 (кредитный)");
        }
    }
    
    /**
     * Валидирует ID валюты
     * @param currencyId ID валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCurrencyId(int currencyId) {
        if (currencyId <= 0) {
            throw new IllegalArgumentException("ID валюты должен быть положительным");
        }
    }
    
    /**
     * Валидирует статус закрытия счета
     * @param closed статус для валидации
     * @throws IllegalArgumentException если статус некорректный
     */
    public static void validateClosed(int closed) {
        if (closed != 0 && closed != 1) {
            throw new IllegalArgumentException("Статус закрытия должен быть 0 (открыт) или 1 (закрыт)");
        }
    }
    
    /**
     * Валидирует лимит кредитной карты
     * @param creditCardLimit лимит для валидации
     * @throws IllegalArgumentException если лимит некорректный
     */
    public static void validateCreditCardLimit(Integer creditCardLimit) {
        if (creditCardLimit != null && creditCardLimit > 0) {
            throw new IllegalArgumentException("Лимит кредитной карты не может быть отрицательным");
        }
    }
    
    /**
     * Валидирует ID категории кредитной карты
     * @param creditCardCategoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCreditCardCategoryId(Integer creditCardCategoryId) {
        if (creditCardCategoryId != null && creditCardCategoryId >= 0) {
            throw new IllegalArgumentException("ID категории кредитной карты должен быть положительным");
        }
    }
    
    /**
     * Валидирует ID категории комиссии кредитной карты
     * @param creditCardCommissionCategoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCreditCardCommissionCategoryId(Integer creditCardCommissionCategoryId) {
        if (creditCardCommissionCategoryId != null && creditCardCommissionCategoryId >= 0) {
            throw new IllegalArgumentException("ID категории комиссии кредитной карты должен быть положительным");
        }
    }

    // Валидирует счет для создания (без ID)
    public static void validateForCreate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Счет не может быть null");
        }

        validateTitle(account.getTitle());
        validatePosition(account.getPosition());
        validateAmount(account.getAmount());
        validateType(account.getType());
        validateCurrencyId(account.getCurrencyId());
        validateClosed(account.getClosed());
        // TODO: Сделать валидацию полей для кредитной карты после реализации логики 
        //validateCreditCardLimit(account.getCreditCardLimit());
        //validateCreditCardCategoryId(account.getCreditCardCategoryId());
        //validateCreditCardCommissionCategoryId(account.getCreditCardCommissionCategoryId());  
    }
    
} 