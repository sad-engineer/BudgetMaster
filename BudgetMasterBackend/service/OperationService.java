package service;

import model.Operation;
import repository.OperationRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
     * Пользователь, выполняющий операции
     */
    private final String user;

    /**
     * Конструктор для сервиса
     * @param operationRepository репозиторий для работы с операциями
     * @param user пользователь, выполняющий операции
     */
    public OperationService(OperationRepository operationRepository, String user) {
        this.operationRepository = operationRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public OperationService(String user) {
        this.operationRepository = new OperationRepository("budget_master.db");
        this.user = user;
    }

    /**
     * Удаляет операцию по id
     * @param id id операции
     * @return true, если удаление успешно
     */
    public boolean delete(int id) {
        return operationRepository.deleteById(id, user);
    }

    /**
     * Создает новую операцию
     * @param type тип операции (1 - расход, 2 - доход)
     * @param date дата операции
     * @param amount сумма операции
     * @param comment комментарий операции
     * @param categoryId ID категории
     * @param accountId ID счета
     * @param currencyId ID валюты
     * @param toAccountId ID целевого счета (для переводов)
     * @param toCurrencyId ID целевой валюты (для переводов)
     * @param toAmount сумма в целевой валюте (для переводов)
     * @return операция
     * @throws IllegalArgumentException если тип операции не равен 1 или 2, дата больше текущего времени, или сумма не больше нуля
     */
    public Operation create(int type, LocalDateTime date, int amount, String comment, int categoryId, int accountId, int currencyId, int toAccountId, int toCurrencyId, int toAmount) {
        // Валидация типа операции
        if (type != 1 && type != 2) {
            throw new IllegalArgumentException("Тип операции должен быть 1 (расход) или 2 (доход), получено: " + type);
        }
        
        // Валидация даты операции
        LocalDateTime now = LocalDateTime.now();
        if (date.isAfter(now)) {
            throw new IllegalArgumentException("Дата операции не может быть больше текущего времени. Получено: " + date + ", текущее время: " + now);
        }
        
        // Валидация суммы операции
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма операции должна быть больше нуля. Получено: " + amount);
        }
        
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
        
        return operationRepository.save(newOperation);
    }

    /**
     * Получает все операции по условию
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций
     */
    public List<Operation> getAll(boolean activeOnly) {
        List<Operation> allOperations = operationRepository.findAll();
        if (!activeOnly) {
            return allOperations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : allOperations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции (не удаленные)
     * @return список активных операций
     */
    public List<Operation> getAll() {
        return getAll(true);
    }

    /**
     * Получает операцию по ID
     * @param id ID операции
     * @return операция
     */
    public Optional<Operation> getById(int id) { 
        return operationRepository.findById(id);
    }

    /**
     * Получает операции за день даты
     * @param date день операций
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций если они были в этот день
     */
    public List<Operation> getByDay(LocalDateTime date, boolean activeOnly) {
        List<Operation> operations = operationRepository.findAllByDate(date);
        if (!activeOnly) {
            return operations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : operations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции за день даты
     * @param date день операций
     * @return список активных операций если они были в этот день
     */
    public List<Operation> getByDay(LocalDateTime date) {
        return getByDay(date, true);
    }

    /**
     * Получает операции по комментарию
     * @param comment комментарий операции
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций с указанным комментарием
     */
    public List<Operation> getByComment(String comment, boolean activeOnly) { 
        List<Operation> operations = operationRepository.findAllByComment(comment);
        if (!activeOnly) {
            return operations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : operations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции по комментарию
     * @param comment комментарий операции
     * @return список активных операций с указанным комментарием
     */
    public List<Operation> getByComment(String comment) { 
        return getByComment(comment, true);
    }

    /**
     * Получает операции по ID категории
     * @param categoryId ID категории
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций с указанным ID категории
     */
    public List<Operation> getByCategoryId(int categoryId, boolean activeOnly) { 
        List<Operation> operations = operationRepository.findAllByCategoryId(categoryId);
        if (!activeOnly) {
            return operations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : operations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции по ID категории
     * @param categoryId ID категории
     * @return список активных операций с указанным ID категории
     */
    public List<Operation> getByCategoryId(int categoryId) { 
        return getByCategoryId(categoryId, true);
    }

    /**
     * Получает операции по ID счета
     * @param accountId ID счета
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций с указанным ID счета
     */
    public List<Operation> getByAccountId(int accountId, boolean activeOnly) { 
        List<Operation> operations = operationRepository.findAllByAccountId(accountId);
        if (!activeOnly) {
            return operations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : operations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции по ID счета
     * @param accountId ID счета
     * @return список активных операций с указанным ID счета
     */
    public List<Operation> getByAccountId(int accountId) { 
        return getByAccountId(accountId, true);
    }

    /**
     * Получает операции по ID валюты
     * @param currencyId ID валюты
     * @param activeOnly если true, возвращает только активные (не удаленные) операции
     * @return список операций с указанным ID валюты
     */
    public List<Operation> getByCurrencyId(int currencyId, boolean activeOnly) { 
        List<Operation> operations = operationRepository.findAllByCurrencyId(currencyId);
        if (!activeOnly) {
            return operations;
        }
        
        // Фильтруем только активные операции
        List<Operation> activeOperations = new ArrayList<>();
        for (Operation operation : operations) {
            if (!isOperationDeleted(operation)) {
                activeOperations.add(operation);
            }
        }
        return activeOperations;
    }

    /**
     * Получает только активные операции по ID валюты
     * @param currencyId ID валюты
     * @return список активных операций с указанным ID валюты
     */
    public List<Operation> getByCurrencyId(int currencyId) { 
        return getByCurrencyId(currencyId, true);
    }

    /**
     * Проверка операции на удаление
     * @param operation класс операции
     * @return true, если операция удалена
     */
    public boolean isOperationDeleted(Operation operation) {
        return operation.getDeleteTime() != null;
    }

    /**
     * Восстанавливает операцию
     * @param restoredOperation операция
     * @return операция
     */
    public Operation restore(Operation restoredOperation) {
        restoredOperation.setDeleteTime(null);
        restoredOperation.setDeletedBy(null);
        restoredOperation.setUpdateTime(LocalDateTime.now());
        restoredOperation.setUpdatedBy(user);
        return operationRepository.update(restoredOperation);
    }

    /**
     * Восстанавливает операцию по id
     * @param id id операции
     * @return операция или null, если операция не найдена
     */
    public Operation restore(int id) {
        Optional<Operation> operationOpt = getById(id);
        if (operationOpt.isPresent()) {
            return restore(operationOpt.get());
        }
        return null;
    }

    /**
     * Устанавливает нового пользователя для операций
     * @param newUser новый пользователь
     */
    public void setUser(String newUser) {
        // Обратите внимание: поле user final, поэтому нужно создать новый экземпляр сервиса
        // или использовать другой подход для смены пользователя
        throw new UnsupportedOperationException("Для смены пользователя создайте новый экземпляр CurrencyService");
    }
} 