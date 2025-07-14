// -*- coding: utf-8 -*-
package validator;

import model.Account;
import validator.BaseEntityValidator;
import validator.CommonValidator;

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
        CommonValidator.validateTitle(account.getTitle(), "Название счета");
        CommonValidator.validatePosition(account.getPosition());
        CommonValidator.validateAccountAmount(account.getAmount());
        CommonValidator.validateAccountType(account.getType());
        CommonValidator.validateCurrencyId(account.getCurrencyId());
        CommonValidator.validateClosedStatus(account.getClosed());
        // TODO: Сделать валидацию полей для кредитной карты после реализации логики 
        //validateCreditCardLimit(account.getCreditCardLimit());
        //validateCreditCardCategoryId(account.getCreditCardCategoryId());
        //validateCreditCardCommissionCategoryId(account.getCreditCardCommissionCategoryId());
    }

    /**
     * Валидирует счет для создания (без ID)
     * @param account счет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Счет не может быть null");
        }

        CommonValidator.validateTitle(account.getTitle(), "Название счета");
        CommonValidator.validatePosition(account.getPosition());
        CommonValidator.validateAccountAmount(account.getAmount());
        CommonValidator.validateAccountType(account.getType());
        CommonValidator.validateCurrencyId(account.getCurrencyId());
        CommonValidator.validateClosedStatus(account.getClosed());
        // TODO: Сделать валидацию полей для кредитной карты после реализации логики 
        //validateCreditCardLimit(account.getCreditCardLimit());
        //validateCreditCardCategoryId(account.getCreditCardCategoryId());
        //validateCreditCardCommissionCategoryId(account.getCreditCardCommissionCategoryId());  
    }
    
    /**
     * Валидирует счет для обновления
     * @param account счет для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Счет не может быть null");
        }

        CommonValidator.validateTitle(account.getTitle(), "Название счета");
        CommonValidator.validatePosition(account.getPosition());
        CommonValidator.validateAccountAmount(account.getAmount());
        CommonValidator.validateAccountType(account.getType());
        CommonValidator.validateCurrencyId(account.getCurrencyId());
        CommonValidator.validateClosedStatus(account.getClosed());
        // TODO: Сделать валидацию полей для кредитной карты после реализации логики 
        //validateCreditCardLimit(account.getCreditCardLimit());
        //validateCreditCardCategoryId(account.getCreditCardCategoryId());
        //validateCreditCardCommissionCategoryId(account.getCreditCardCommissionCategoryId());
    }
} 