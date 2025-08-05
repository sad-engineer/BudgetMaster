
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Account;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.repository.OperationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Operation
 */
public class OperationService {
    
    private final OperationRepository operationRepository;
    private final AccountService accountService;
    private final CurrencyService currencyService;
    private final ExecutorService executorService;
    private final String user;
    
    public OperationService(Context context, String user) {
        this.operationRepository = new OperationRepository(context);
        this.accountService = new AccountService(context, user);
        this.currencyService = new CurrencyService(context, user);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }
    
    // Получить все операции
    public LiveData<List<Operation>> getAllOperations() {
        return operationRepository.getAllOperations();
    }
    
    // Получить операции по типу
    public LiveData<List<Operation>> getOperationsByType(String type) {
        return operationRepository.getOperationsByType(type);
    }
    
    // Получить операции по счету
    public LiveData<List<Operation>> getOperationsByAccount(int accountId) {
        return operationRepository.getOperationsByAccount(accountId);
    }
    
    // Получить операции по категории
    public LiveData<List<Operation>> getOperationsByCategory(int categoryId) {
        return operationRepository.getOperationsByCategory(categoryId);
    }
    
    // Получить операции по валюте
    public LiveData<List<Operation>> getOperationsByCurrency(int currencyId) {
        return operationRepository.getOperationsByCurrency(currencyId);
    }
    
    // Получить операции за день
    public LiveData<List<Operation>> getOperationsByDay(LocalDateTime date) {
        return operationRepository.getOperationsByDay(date);
    }
    
    // Получить операции по комментарию
    public LiveData<List<Operation>> getOperationsByComment(String comment) {
        return operationRepository.getOperationsByComment(comment);
    }
    
    // Получить операцию по ID
    public LiveData<Operation> getOperationById(int id) {
        return operationRepository.getOperationById(id);
    }
    
    // Создать новую операцию
    public void createOperation(String type, LocalDateTime date, int amount, String description, 
                              int categoryId, int accountId, int currencyId) {
        Operation operation = new Operation();
        operation.setType(type);
        operation.setOperationDate(date);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setCategoryId(categoryId);
        operation.setAccountId(accountId);
        operation.setCurrencyId(currencyId);
        
        operationRepository.insertOperation(operation, user);
    }
    
    // Создать операцию перевода
    public void createTransferOperation(LocalDateTime date, int amount, String description,
                                     int accountId, int currencyId, int toAccountId, int toCurrencyId, int toAmount) {
        Operation operation = new Operation();
        operation.setType("transfer");
        operation.setOperationDate(date);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setAccountId(accountId);
        operation.setCurrencyId(currencyId);
        operation.setToAccountId(toAccountId);
        operation.setToCurrencyId(toCurrencyId);
        operation.setToAmount((int)toAmount);
        
        operationRepository.insertOperation(operation, user);
    }
    
    // Обновить операцию
    public void updateOperation(Operation operation) {
        operationRepository.updateOperation(operation, user);
    }
    
    // Удалить операцию (soft delete)
    public void deleteOperation(int operationId) {
        operationRepository.deleteOperation(operationId, user);
    }
    
    // Восстановить удаленную операцию
    public void restoreOperation(int operationId) {
        executorService.execute(() -> {
            // Получаем удаленную операцию
            Operation deletedOperation = operationRepository.getOperationById(operationId).getValue();
            if (deletedOperation == null || !deletedOperation.isDeleted()) {
                return; // Операция не найдена или уже активна
            }
            
            // Очищаем поля удаления
            deletedOperation.setDeleteTime(null);
            deletedOperation.setDeletedBy(null);
            deletedOperation.setUpdateTime(LocalDateTime.now());
            deletedOperation.setUpdatedBy(user);
            
            // Обновляем операцию в базе
            operationRepository.updateOperation(deletedOperation, user);
        });
    }
    
    // Получить операции с автоматическим получением счетов и валют по названиям
    public LiveData<Operation> createOperationWithTitles(String type, LocalDateTime date, int amount, 
                                                       String description, int categoryId, 
                                                       String accountTitle, String currencyTitle) {
        MutableLiveData<Operation> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Получаем счет и валюту по названиям
            Account account = accountService.getAccountByTitle(accountTitle).getValue();
            Currency currency = currencyService.getByTitle(currencyTitle).getValue();
            
            if (account != null && currency != null) {
                Operation operation = new Operation();
                operation.setType(type);
                operation.setOperationDate(date);
                operation.setAmount(amount);
                operation.setDescription(description);
                operation.setCategoryId(categoryId);
                operation.setAccountId(account.getId());
                operation.setCurrencyId(currency.getId());
                
                operationRepository.insertOperation(operation, user);
                liveData.postValue(operation);
            }
        });
        return liveData;
    }
    
    // Создать перевод с автоматическим получением счетов и валют по названиям
    public LiveData<Operation> createTransferWithTitles(LocalDateTime date, int amount, String description,
                                                      String accountTitle, String currencyTitle,
                                                      String toAccountTitle, String toCurrencyTitle, int toAmount) {
        MutableLiveData<Operation> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Получаем счета и валюты по названиям
            Account account = accountService.getAccountByTitle(accountTitle).getValue();
            Currency currency = currencyService.getByTitle(currencyTitle).getValue();
            Account toAccount = accountService.getAccountByTitle(toAccountTitle).getValue();
            Currency toCurrency = currencyService.getByTitle(toCurrencyTitle).getValue();
            
            if (account != null && currency != null && toAccount != null && toCurrency != null) {
                Operation operation = new Operation();
                operation.setType("transfer");
                operation.setOperationDate(date);
                operation.setAmount(amount);
                operation.setDescription(description);
                operation.setAccountId(account.getId());
                operation.setCurrencyId(currency.getId());
                operation.setToAccountId(toAccount.getId());
                operation.setToCurrencyId(toCurrency.getId());
                operation.setToAmount((int)toAmount);
                
                operationRepository.insertOperation(operation, user);
                liveData.postValue(operation);
            }
        });
        return liveData;
    }
    
    // Получить счет по названию через AccountService
    public LiveData<Account> getAccount(String accountTitle) {
        return accountService.getAccountByTitle(accountTitle);
    }
    
    // Получить счет по названию (для внутреннего использования)
    public LiveData<Account> getAccountByTitle(String accountTitle) {
        return accountService.getAccountByTitle(accountTitle);
    }
    
    // Получить валюту по названию через CurrencyService
    public LiveData<Currency> getCurrency(String currencyTitle) {
        return currencyService.getByTitle(currencyTitle);
    }
    
    
    // Получить общую сумму операций по типу
    public LiveData<Integer> getTotalAmountByType(String type) {
        return operationRepository.getTotalAmountByType(type);
    }
    
    // Получить общую сумму операций по счету
    public LiveData<Integer> getTotalAmountByAccount(int accountId) {
        return operationRepository.getTotalAmountByAccount(accountId);
    }
    
    // Получить общую сумму операций по категории
    public LiveData<Integer> getTotalAmountByCategory(int categoryId) {
        return operationRepository.getTotalAmountByCategory(categoryId);
    }
    
    // Получить общую сумму операций по валюте
    public LiveData<Integer> getTotalAmountByCurrency(int currencyId) {
        return operationRepository.getTotalAmountByCurrency(currencyId);
    }
    
    // Получить количество операций
    public LiveData<Integer> getOperationsCount() {
        return operationRepository.getOperationsCount();
    }
    
    // Валидация операции
    public boolean validateOperation(Operation operation) {
        if (operation.getType() == null || operation.getType().trim().isEmpty()) {
            return false;
        }
        if (!operation.getType().equals("income") && !operation.getType().equals("expense") && !operation.getType().equals("transfer")) {
            return false;
        }
        if (operation.getAmount() <= 0) {
            return false;
        }
        if (operation.getAccountId() <= 0) {
            return false;
        }
        if (operation.getCategoryId() <= 0) {
            return false;
        }
        if (operation.getCurrencyId() <= 0) {
            return false;
        }
        if (operation.getOperationDate() == null) {
            return false;
        }
        return true;
    }
    
    // Проверить, является ли операция расходом
    public boolean isExpense(Operation operation) {
        return operation != null && operation.isExpense();
    }
    
    // Проверить, является ли операция доходом
    public boolean isIncome(Operation operation) {
        return operation != null && operation.isIncome();
    }
    
    // Проверить, является ли операция переводом
    public boolean isTransfer(Operation operation) {
        return operation != null && operation.isTransfer();
    }
    
    // Получить баланс по счету
    public LiveData<Integer> getAccountBalance(int accountId) {
        return operationRepository.getAccountBalance(accountId);
    }
    
    // Получить статистику по операциям
    public LiveData<OperationStatistics> getOperationStatistics() {
        return operationRepository.getOperationStatistics();
    }
    
    // Класс для статистики операций
    public static class OperationStatistics {
        private int totalIncome;
        private int totalExpense;
        private int totalTransfer;
        private int operationsCount;
        
        public OperationStatistics(int totalIncome, int totalExpense, int totalTransfer, int operationsCount) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.totalTransfer = totalTransfer;
            this.operationsCount = operationsCount;
        }
        
        public int getTotalIncome() { return totalIncome; }
        public int getTotalExpense() { return totalExpense; }
        public int getTotalTransfer() { return totalTransfer; }
        public int getOperationsCount() { return operationsCount; }
        public int getNetAmount() { return totalIncome - totalExpense; }
    }
} 