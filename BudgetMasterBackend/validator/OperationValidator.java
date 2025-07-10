// -*- coding: utf-8 -*-
package validator;

import model.Operation;
import validator.BaseEntityValidator;
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
        validateType(operation.getType());
        validateDate(operation.getDate());
        validateAmount(operation.getAmount());
        validateComment(operation.getComment());
        validateCategoryId(operation.getCategoryId());
        validateAccountId(operation.getAccountId());
        validateCurrencyId(operation.getCurrencyId());
        validateToAccountId(operation.getToAccountId());
        validateToCurrencyId(operation.getToCurrencyId());
        validateToAmount(operation.getToAmount());
        
        // Валидация бизнес-логики
        validateTransferFields(operation);
    }
    
    /**
     * Валидирует тип операции
     * @param type тип для валидации
     * @throws IllegalArgumentException если тип некорректный
     */
    public static void validateType(int type) {
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("Тип операции должен быть 1 (расход) или 2 (доход)");
        }
    }
    
    /**
     * Валидирует дату операции
     * @param date дата для валидации
     * @throws IllegalArgumentException если дата некорректная
     */
    public static void validateDate(LocalDateTime date) {
        if (date == null) {
            throw new IllegalArgumentException("Дата операции не может быть null");
        }
        
        if (date.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата операции не может быть в будущем");
        }
    }
    
    /**
     * Валидирует сумму операции
     * @param amount сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма операции должна быть больше нуля");
        }
    }
    
    /**
     * Валидирует комментарий операции
     * @param comment комментарий для валидации
     * @throws IllegalArgumentException если комментарий некорректный
     */
    public static void validateComment(String comment) {
        if (comment != null && comment.length() > 500) {
            throw new IllegalArgumentException("Комментарий операции не может быть длиннее 500 символов");
        }
        
        // Проверка на допустимые символы (буквы, цифры, пробелы, знаки препинания)
        if (comment != null && !comment.matches("^[а-яА-Яa-zA-Z0-9\\s\\-\\.,!?;:()]+$")) {
            throw new IllegalArgumentException("Комментарий операции содержит недопустимые символы");
        }
    }
    
    /**
     * Валидирует ID категории
     * @param categoryId ID категории для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("ID категории должен быть положительным");
        }
    }
    
    /**
     * Валидирует ID счета
     * @param accountId ID счета для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateAccountId(int accountId) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("ID счета должен быть положительным");
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
     * Валидирует ID целевого счета
     * @param toAccountId ID целевого счета для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateToAccountId(Integer toAccountId) {
        if (toAccountId != null && toAccountId <= 0) {
            throw new IllegalArgumentException("ID целевого счета должен быть положительным");
        }
    }
    
    /**
     * Валидирует ID целевой валюты
     * @param toCurrencyId ID целевой валюты для валидации
     * @throws IllegalArgumentException если ID некорректный
     */
    public static void validateToCurrencyId(Integer toCurrencyId) {
        if (toCurrencyId != null && toCurrencyId <= 0) {
            throw new IllegalArgumentException("ID целевой валюты должен быть положительным");
        }
    }
    
    /**
     * Валидирует целевую сумму
     * @param toAmount целевая сумма для валидации
     * @throws IllegalArgumentException если сумма некорректная
     */
    public static void validateToAmount(Integer toAmount) {
        if (toAmount != null && toAmount <= 0) {
            throw new IllegalArgumentException("Целевая сумма должна быть больше нуля");
        }
    }
    
    /**
     * Валидирует поля для перевода между счетами
     * @param operation операция для валидации
     * @throws IllegalArgumentException если поля некорректные
     */
    public static void validateTransferFields(Operation operation) {
        // Если указан целевой счет, должны быть указаны и другие поля перевода
        if (operation.getToAccountId() != null) {
            if (operation.getToCurrencyId() == null) {
                throw new IllegalArgumentException("При указании целевого счета должна быть указана целевая валюта");
            }
            if (operation.getToAmount() == null) {
                throw new IllegalArgumentException("При указании целевого счета должна быть указана целевая сумма");
            }
        }
        
        // Если указана целевая валюта, должны быть указаны и другие поля перевода
        if (operation.getToCurrencyId() != null) {
            if (operation.getToAccountId() == null) {
                throw new IllegalArgumentException("При указании целевой валюты должен быть указан целевой счет");
            }
            if (operation.getToAmount() == null) {
                throw new IllegalArgumentException("При указании целевой валюты должна быть указана целевая сумма");
            }
        }
        
        // Если указана целевая сумма, должны быть указаны и другие поля перевода
        if (operation.getToAmount() != null) {
            if (operation.getToAccountId() == null) {
                throw new IllegalArgumentException("При указании целевой суммы должен быть указан целевой счет");
            }
            if (operation.getToCurrencyId() == null) {
                throw new IllegalArgumentException("При указании целевой суммы должна быть указана целевая валюта");
            }
        }
        
        // Проверка, что счет не переводит сам в себя
        if (operation.getToAccountId() != null && operation.getAccountId() == operation.getToAccountId()) {
            throw new IllegalArgumentException("Нельзя переводить деньги с счета на тот же счет");
        }
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
        
        validateType(operation.getType());
        validateDate(operation.getDate());
        validateAmount(operation.getAmount());
        validateComment(operation.getComment());
        validateCategoryId(operation.getCategoryId());
        validateAccountId(operation.getAccountId());
        validateCurrencyId(operation.getCurrencyId());
        validateToAccountId(operation.getToAccountId());
        validateToCurrencyId(operation.getToCurrencyId());
        validateToAmount(operation.getToAmount());
        validateTransferFields(operation);
    }
    
    /**
     * Валидирует операцию для обновления
     * @param operation операция для валидации
     * @throws IllegalArgumentException если валидация не прошла
     */
    public static void validateForUpdate(Operation operation) {
        validate(operation);
    }
    
    /**
     * Проверяет, что операция не превышает разумные пределы
     * @param operation операция для проверки
     * @throws IllegalArgumentException если операция превышает пределы
     */
    public static void validateReasonableAmount(Operation operation) {
        if (operation.getAmount() > 1000000000) { // 1 миллиард
            throw new IllegalArgumentException("Сумма операции не может превышать 1 миллиард");
        }
        
        if (operation.getToAmount() != null && operation.getToAmount() > 1000000000) {
            throw new IllegalArgumentException("Целевая сумма операции не может превышать 1 миллиард");
        }
    }
} 