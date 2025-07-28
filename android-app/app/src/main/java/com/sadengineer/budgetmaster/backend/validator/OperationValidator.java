// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.validator;

import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.constants.ValidationConstants;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import java.time.LocalDateTime;

/**
 * Валидатор для операции
 */
public class OperationValidator {
    
    /**
     * Валидирует операцию
     * @param operation операция для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validate(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Операция не может быть null");
        }

        // Валидация базовых полей
        BaseEntityValidator.validate(operation);

        // Валидация специфичных полей
        CommonValidator.validateOperationType(operation.getType());
        CommonValidator.validateOperationDate(operation.getOperationDate());
        CommonValidator.validateOperationAmount(operation.getAmount());
        CommonValidator.validateComment(operation.getDescription());
        CommonValidator.validateCategoryId(operation.getCategoryId());
        CommonValidator.validateAccountId(operation.getAccountId());
        CommonValidator.validateCurrencyId(operation.getCurrencyId());
        // TODO: Сделать валидацию полей для перевода между счетами после реализации логики
        //validateToAccountId(operation.getToAccountId());
        //validateToCurrencyId(operation.getToCurrencyId());
        //validateToAmount(operation.getToAmount());
        //validateTransferFields(operation);
    }

    /**
     * Валидирует операцию для создания (без ID)
     * @param operation операция для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForCreate(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Операция не может быть null");
        }

        CommonValidator.validateOperationType(operation.getType());
        CommonValidator.validateOperationDate(operation.getOperationDate());
        CommonValidator.validateOperationAmount(operation.getAmount());
        CommonValidator.validateComment(operation.getDescription());
        CommonValidator.validateCategoryId(operation.getCategoryId());
        CommonValidator.validateAccountId(operation.getAccountId());
        CommonValidator.validateCurrencyId(operation.getCurrencyId());
        // TODO: Сделать валидацию полей для перевода между счетами после реализации логики
        //validateToAccountId(operation.getToAccountId());
        //validateToCurrencyId(operation.getToCurrencyId());
        //validateToAmount(operation.getToAmount());
        //validateTransferFields(operation);
    }
    
    /**
     * Валидирует операцию для обновления
     * @param operation операция для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Операция не может быть null");
        }

        CommonValidator.validateOperationType(operation.getType());
        CommonValidator.validateOperationDate(operation.getOperationDate());
        CommonValidator.validateOperationAmount(operation.getAmount());
        CommonValidator.validateComment(operation.getDescription());
        CommonValidator.validateCategoryId(operation.getCategoryId());
        CommonValidator.validateAccountId(operation.getAccountId());
        CommonValidator.validateCurrencyId(operation.getCurrencyId());
        // TODO: Сделать валидацию полей для перевода между счетами после реализации логики
        //validateToAccountId(operation.getToAccountId());
        //validateToCurrencyId(operation.getToCurrencyId());
        //validateToAmount(operation.getToAmount());
        //validateTransferFields(operation);
    }
} 