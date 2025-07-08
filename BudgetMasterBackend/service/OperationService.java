package service;

import model.Operation;
import repository.OperationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OperationService {
    private final OperationRepository operationRepository;

    public OperationService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    public Operation createOperation(Operation operation) {
        // Бизнес-валидация перед сохранением
        validateOperation(operation);
        
        // Устанавливаем время создания, если не установлено
        if (operation.getCreateTime() == null) {
            operation.setCreateTime(LocalDateTime.now());
        }
        
        if (operation.getUpdateTime() == null) {
            operation.setUpdateTime(LocalDateTime.now());
        }
        
        // Сохраняем операцию
        return operationRepository.save(operation);
    }

    public Optional<Operation> getOperationById(int id) {
        return operationRepository.findById(id);
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    /**
     * Получает все операции, отсортированные по дате (сначала новые)
     * @return список операций, отсортированных по дате
     */
    public List<Operation> getAllOperationsOrderedByDate() {
        return operationRepository.findAllOrderedByDate();
    }

    public Operation updateOperation(Operation operation) {
        // Бизнес-валидация перед обновлением
        validateOperation(operation);
        
        // Обновляем время изменения
        operation.setUpdateTime(LocalDateTime.now());
        
        // Обновляем операцию
        return operationRepository.update(operation);
    }

    public boolean deleteOperation(int id) {
        return operationRepository.delete(id);
    }

    public boolean deleteOperation(int id, String deletedBy) {
        return operationRepository.delete(id, deletedBy);
    }

    public boolean restoreOperation(int id) {
        return operationRepository.restore(id);
    }

    public List<Operation> getDeletedOperations() {
        return operationRepository.findDeleted();
    }

    /**
     * Валидация операции
     * @param operation операция для валидации
     * @throws IllegalArgumentException если операция невалидна
     */
    private void validateOperation(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Операция не может быть null");
        }
        
        if (operation.getType() <= 0) {
            throw new IllegalArgumentException("Тип операции должен быть положительным числом");
        }
        
        if (operation.getDate() == null) {
            throw new IllegalArgumentException("Дата операции не может быть null");
        }
        
        if (operation.getAmount() <= 0) {
            throw new IllegalArgumentException("Сумма операции должна быть положительной");
        }
        
        if (operation.getCategoryId() <= 0) {
            throw new IllegalArgumentException("ID категории должен быть положительным числом");
        }
        
        if (operation.getAccountId() <= 0) {
            throw new IllegalArgumentException("ID счета должен быть положительным числом");
        }
        
        if (operation.getCurrencyId() <= 0) {
            throw new IllegalArgumentException("ID валюты должен быть положительным числом");
        }
        
        // Проверка для переводов между счетами
        if (operation.getType() == 3) { // Предполагаем, что тип 3 - это перевод
            if (operation.getToAccountId() == null || operation.getToAccountId() <= 0) {
                throw new IllegalArgumentException("Для перевода должен быть указан счет назначения");
            }
            
            if (operation.getToCurrencyId() == null || operation.getToCurrencyId() <= 0) {
                throw new IllegalArgumentException("Для перевода должна быть указана валюта назначения");
            }
            
            if (operation.getToAmount() == null || operation.getToAmount() <= 0) {
                throw new IllegalArgumentException("Для перевода должна быть указана сумма назначения");
            }
        }
    }
} 