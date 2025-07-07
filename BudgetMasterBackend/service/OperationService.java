package service;

import model.Operation;
import repository.OperationRepository;

import java.util.List;
import java.util.Optional;

public class OperationService {
    private final OperationRepository operationRepository;

    public OperationService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    public Operation createOperation(Operation operation) {
        // Здесь можно добавить бизнес-валидацию перед сохранением
        return operationRepository.save(operation);
    }

    public Optional<Operation> getOperationById(int id) {
        return operationRepository.findById(id);
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public Operation updateOperation(Operation operation) {
        // Здесь можно добавить бизнес-валидацию перед обновлением
        return operationRepository.update(operation);
    }

    public boolean deleteOperation(int id) {
        return operationRepository.delete(id);
    }
} 