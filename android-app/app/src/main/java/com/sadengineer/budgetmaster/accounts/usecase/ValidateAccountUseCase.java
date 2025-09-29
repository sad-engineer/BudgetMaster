package com.sadengineer.budgetmaster.accounts.usecase;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.validator.AccountValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Use Case для валидации счета
 */
public class ValidateAccountUseCase implements BaseUseCaseWithParams<Account, ValidationResult> {
    
    private final AccountValidator validator;
    
    public ValidateAccountUseCase() {
        this.validator = new AccountValidator();
    }
    
    @Override
    public ValidationResult execute(Account account) {
        if (account == null) {
            return ValidationResult.error("Счет не может быть null");
        }
        
        List<String> errors = new ArrayList<>();
        
        try {
            // Валидация названия
            validator.validateTitle(account.getTitle());
        } catch (IllegalArgumentException e) {
            errors.add("Название: " + e.getMessage());
        }
        
        try {
            // Валидация суммы
            validator.validateAmount(account.getAmount());
        } catch (IllegalArgumentException e) {
            errors.add("Сумма: " + e.getMessage());
        }
        
        try {
            // Валидация типа
            validator.validateType(account.getType());
        } catch (IllegalArgumentException e) {
            errors.add("Тип: " + e.getMessage());
        }
        
        try {
            // Валидация статуса
            validator.validateClosed(account.getClosed());
        } catch (IllegalArgumentException e) {
            errors.add("Статус: " + e.getMessage());
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.error(String.join("; ", errors));
        }
    }
    
    /**
     * Результат валидации
     * этот класс используется для валидации счета
     * он используется для валидации счета
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        private ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean hasError() {
            return !isValid;
        }
    }
}
