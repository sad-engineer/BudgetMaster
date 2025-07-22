// -*- coding: utf-8 -*-

package com.sadengineer.budgetmaster.backend.service;

import com.sadengineer.budgetmaster.backend.model.Account;
import com.sadengineer.budgetmaster.backend.model.Currency;
import com.sadengineer.budgetmaster.backend.model.Operation;
import com.sadengineer.budgetmaster.backend.repository.OperationRepository;
import com.sadengineer.budgetmaster.backend.validator.OperationValidator;
import com.sadengineer.budgetmaster.backend.validator.BaseEntityValidator;
import com.sadengineer.budgetmaster.backend.validator.CommonValidator;
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с операциями
 */
public class OperationService {
    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;
    
    /**
     * Сервис для работы со счетами
     */
    private final AccountService accountService;
    
    /**
     * Сервис для работы с валютами
     */
    private final CurrencyService currencyService;
    
    /**
     * Пользователь, выполняющий операции
     */
    private final String user;

    /**
     * Конструктор для сервиса
     * @param operationRepository репозиторий для работы с операциями
     * @param accountService сервис для работы со счетами
     * @param currencyService сервис для работы с валютами
     * @param user пользователь, выполняющий операции
     */
    public OperationService(OperationRepository operationRepository, AccountService accountService, CurrencyService currencyService, String user) {
        this.operationRepository = operationRepository;
        this.accountService = accountService;
        this.currencyService = currencyService;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозиториев и сервисов
     * @param user пользователь, выполняющий операции
     */
    public OperationService(String user) {
        this.operationRepository = new OperationRepository(ServiceConstants.DEFAULT_DATABASE_NAME);
        this.accountService = new AccountService(user);
        this.currencyService = new CurrencyService(user);
        this.user = user;
    }

    /**
     * Удаляет операцию по id
     * @param id id операции
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id) {
        BaseEntityValidator.validatePositiveId(id, "ID операции");
        return operationRepository.deleteById(id, user);
    }

    /**
     * Создает новую операцию без валидации (для внутреннего использования)
     * @param type тип операции (1 - расход, 2 - доход)
     * @param date дата операции
     * @param amount сумма операции в копейках валюты
     * @param comment комментарий операции
     * @param categoryId ID категории
     * @param accountId ID счета
     * @param currencyId ID валюты
     * @param toAccountId ID целевого счета (для переводов)
     * @param toCurrencyId ID целевой валюты (для переводов)
     * @param toAmount сумма в целевой валюте (для переводов)
     * @return операция
     */
    private Operation create(int type, LocalDateTime date, int amount, String comment, int categoryId, int accountId, int currencyId, Integer toAccountId, Integer toCurrencyId, Integer toAmount) {
        Operation newOperation = new Operation();
        newOperation.setType(type);
        newOperation.setDate(date);
        newOperation.setAmount(amount);
        newOperation.setComment(comment);
        newOperation.setCategoryId(categoryId);
        newOperation.setAccountId(accountId);
        newOperation.setCurrencyId(currencyId);
        newOperation.setToAccountId(toAccountId);
        newOperation.setToCurrencyId(toCurrencyId);
        newOperation.setToAmount(toAmount);
        newOperation.setCreateTime(LocalDateTime.now());
        newOperation.setCreatedBy(user);
        newOperation.setUpdateTime(LocalDateTime.now());
        newOperation.setUpdatedBy(user);

        // Валидация операции перед сохранением
        OperationValidator.validate(newOperation);
        
        return operationRepository.save(newOperation);
    }

    /**
     * Получает операцию по ID. 
     * Если операция с таким ID существует, возвращает ее.
     * Если операция с таким ID существует, но удалена, восстанавливает ее.
     * Если операция с таким ID не существует, вернет null.
     * @param id ID операции
     * @return операция
     */
    public Operation get(Integer id) { 
        BaseEntityValidator.validatePositiveId(id, "ID операции");
        Optional<Operation> operation = operationRepository.findById(id);
        if (operation.isPresent()) {
            Operation operationObj = operation.get();
            if (operationObj.isDeleted()) {
                return restore(operationObj);
            }
            return operationObj;
        }
        return null;
    }

    /**
     * Получает все операции
     * @return список операций
     */
    public List<Operation> getAll() {
        return operationRepository.findAll();
    }

    /**
     * Получает операции за день даты
     * @param date день операций
     * @return список операций если они были в этот день
     */
    public List<Operation> getAllByDay(LocalDateTime date) {
        return operationRepository.findAllByDate(date);
    }

    /**
     * Получает операции по комментарию
     * @param comment комментарий операции
     * @return список операций с указанным комментарием
     */
    public List<Operation> getAllByComment(String comment) { 
        return operationRepository.findAllByComment(comment);
    }

    /**
     * Получает операции по ID категории
     * @param categoryId ID категории
     * @return список операций с указанным ID категории
     */
    public List<Operation> getAllByCategoryId(Integer categoryId) { 
        CommonValidator.validateCategoryId(categoryId);
        return operationRepository.findAllByCategoryId(categoryId);
    }

    /**
     * Получает операции по ID счета
     * @param accountId ID счета
     * @return список операций с указанным ID счета
     */
    public List<Operation> getAllByAccountId(Integer accountId) { 
        CommonValidator.validateAccountId(accountId);
        return operationRepository.findAllByAccountId(accountId);
    }

    /**
     * Получает операции по ID валюты
     * @param currencyId ID валюты
     * @return список операций с указанным ID валюты
     */
    public List<Operation> getAllByCurrencyId(Integer currencyId) { 
        CommonValidator.validateCurrencyId(currencyId);
        return operationRepository.findAllByCurrencyId(currencyId);
    }

    /**
     * Восстанавливает операцию (удаляет данные в полях deletedBy и deleteTime)
     * Для внутреннего использования
     * @param restoredOperation операция для восстановления
     * @return восстановленная операция
     */
    private Operation restore(Operation restoredOperation) {
        restoredOperation.setDeleteTime(null);
        restoredOperation.setDeletedBy(null);
        return update(restoredOperation);
    }

    /**
     * Обновляет операцию без новых параметров (для внутреннего использования)
     * @param updatedOperation операция для обновления
     * @return обновленная операция
     */
    private Operation update(Operation updatedOperation) {
        updatedOperation.setUpdateTime(LocalDateTime.now());
        updatedOperation.setUpdatedBy(user);
        
        return operationRepository.update(updatedOperation);
    }

    /**
     * Обновляет операцию. 
     * 
     * @param updatedOperation операция для обновления
     * @param newType новое значение типа операции (может быть null)
     * @param newDate новое значение даты операции (может быть null)
     * @param newAmount новое значение суммы операции (может быть null)
     * @param newComment новое значение комментария операции (может быть null)
     * @param newCategoryId новое значение ID категории (может быть null)
     * @param newAccountId новое значение ID счета (может быть null)
     * @param newCurrencyId новое значение ID валюты (может быть null)
     * @param newToAccountId новое значение ID целевого счета (может быть null)
     * @param newToCurrencyId новое значение ID целевой валюты (может быть null)
     * @param newToAmount новое значение суммы в целевой валюте (может быть null)
     * @return обновленная операция
     */
    public Operation update(Operation updatedOperation, 
                           Integer newType,
                           LocalDateTime newDate,
                           Integer newAmount,
                           String newComment,
                           Integer newCategoryId,
                           Integer newAccountId,
                           Integer newCurrencyId,
                           Integer newToAccountId,
                           Integer newToCurrencyId,
                           Integer newToAmount) {
        BaseEntityValidator.validate(updatedOperation);
        
        if (newType != null) {
            CommonValidator.validateOperationType(newType);
            updatedOperation.setType(newType);
        }
        
        if (newDate != null) {
            updatedOperation.setDate(newDate);
        }
        
        if (newAmount != null) {
            CommonValidator.validateOperationAmount(newAmount);
            updatedOperation.setAmount(newAmount);
        }
        
        if (newComment != null) {
            updatedOperation.setComment(newComment);
        }
        
        if (newCategoryId != null) {
            CommonValidator.validateCategoryId(newCategoryId);
            updatedOperation.setCategoryId(newCategoryId);
        }
        
        if (newAccountId != null) {
            CommonValidator.validateAccountId(newAccountId);
            updatedOperation.setAccountId(newAccountId);
        }
        
        if (newCurrencyId != null) {
            CommonValidator.validateCurrencyId(newCurrencyId);
            updatedOperation.setCurrencyId(newCurrencyId);
        }
        
        if (newToAccountId != null) {
            CommonValidator.validateAccountId(newToAccountId);
            updatedOperation.setToAccountId(newToAccountId);
        }
        
        if (newToCurrencyId != null) {
            CommonValidator.validateCurrencyId(newToCurrencyId);
            updatedOperation.setToCurrencyId(newToCurrencyId);
        }
        
        if (newToAmount != null) {
            CommonValidator.validateOperationAmount(newToAmount);
            updatedOperation.setToAmount(newToAmount);
        }

        if (updatedOperation.isDeleted()) {
            return restore(updatedOperation);
        }
        
        // Проверяем, был ли задан хотя бы один параметр для обновления
        if (newType != null || newDate != null || newAmount != null || newComment != null || 
            newCategoryId != null || newAccountId != null || newCurrencyId != null || 
            newToAccountId != null || newToCurrencyId != null || newToAmount != null) {
            return update(updatedOperation);
        }
        
        // Если ни один параметр не задан, возвращаем null
        return null;
    }

    

    /**
     * Получает счет по названию через AccountService
     * @param accountTitle название счета
     * @return счет
     */
    public Account getAccount(String accountTitle) {
        return accountService.get(accountTitle);
    }
    
    /**
     * Получает валюту по названию через CurrencyService
     * @param currencyTitle название валюты
     * @return валюта
     */
    public Currency getCurrency(String currencyTitle) {
        return currencyService.get(currencyTitle);
    }

    /**
     * Получает валюту по ID через CurrencyService
     * @param currencyId ID валюты
     * @return валюта
     */
    public Currency getCurrencyById(Integer currencyId) {
        return currencyService.get(currencyId);
    }

    /**
     * Создает операцию с автоматическим получением счетов и валют по названиям
     * @param type тип операции (1 - расход, 2 - доход)
     * @param date дата операции
     * @param amount сумма операции
     * @param comment комментарий операции
     * @param categoryId ID категории
     * @param accountTitle название счета
     * @param currencyTitle название валюты
     * @param toAccountTitle название целевого счета (для переводов, может быть null)
     * @param toCurrencyTitle название целевой валюты (для переводов, может быть null)
     * @param toAmount сумма в целевой валюте (для переводов, может быть null)
     * @return операция
     */
    public Operation createWithTitles(int type, LocalDateTime date, int amount, String comment, int categoryId, 
                                    String accountTitle, String currencyTitle, 
                                    String toAccountTitle, String toCurrencyTitle, Integer toAmount) {
        
        // Получаем счет и валюту по названиям
        Account account = getAccount(accountTitle);
        Currency currency = getCurrency(currencyTitle);
        
        Integer toAccountId = null;
        Integer toCurrencyId = null;
        
        // Если указаны целевые параметры, получаем их
        if (toAccountTitle != null && toCurrencyTitle != null && toAmount != null) {
            Account toAccount = getAccount(toAccountTitle);
            Currency toCurrency = getCurrency(toCurrencyTitle);
            toAccountId = toAccount.getId();
            toCurrencyId = toCurrency.getId();
        }
        
        return create(type, date, amount, comment, categoryId, account.getId(), currency.getId(), 
                     toAccountId, toCurrencyId, toAmount);
    }
} 