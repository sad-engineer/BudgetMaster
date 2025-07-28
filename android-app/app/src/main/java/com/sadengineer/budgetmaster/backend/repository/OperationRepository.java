package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.dao.OperationDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Operation;
import com.sadengineer.budgetmaster.backend.service.OperationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository класс для работы с Operation Entity
 */
public class OperationRepository {
    
    private final OperationDao operationDao;
    private final ExecutorService executorService;
    
    public OperationRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.operationDao = database.operationDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    // Получить все операции
    public LiveData<List<Operation>> getAllOperations() {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getAllOperations();
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции по типу
    public LiveData<List<Operation>> getOperationsByType(String type) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByType(type);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции по счету
    public LiveData<List<Operation>> getOperationsByAccount(int accountId) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByAccount(accountId);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции по категории
    public LiveData<List<Operation>> getOperationsByCategory(int categoryId) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByCategory(categoryId);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции по валюте
    public LiveData<List<Operation>> getOperationsByCurrency(int currencyId) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByCurrency(currencyId);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции за день
    public LiveData<List<Operation>> getOperationsByDay(LocalDateTime date) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByDay(date);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операции по комментарию
    public LiveData<List<Operation>> getOperationsByComment(String comment) {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getOperationsByComment(comment);
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Получить операцию по ID
    public LiveData<Operation> getOperationById(int id) {
        MutableLiveData<Operation> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Operation operation = operationDao.getOperationById(id);
            liveData.postValue(operation);
        });
        return liveData;
    }
    
    // Вставить новую операцию
    public void insertOperation(Operation operation, String createdBy) {
        executorService.execute(() -> {
            operation.setCreateTime(LocalDateTime.now());
            operation.setCreatedBy(createdBy);
            operation.setUpdateTime(LocalDateTime.now());
            operation.setUpdatedBy(createdBy);
            operationDao.insertOperation(operation);
        });
    }
    
    // Обновить операцию
    public void updateOperation(Operation operation, String updatedBy) {
        executorService.execute(() -> {
            operation.setUpdateTime(LocalDateTime.now());
            operation.setUpdatedBy(updatedBy);
            operationDao.updateOperation(operation);
        });
    }
    
    // Удалить операцию (soft delete)
    public void deleteOperation(int operationId, String deletedBy) {
        executorService.execute(() -> {
            operationDao.softDeleteOperation(operationId, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Получить все удаленные операции
    public LiveData<List<Operation>> getAllDeletedOperations() {
        MutableLiveData<List<Operation>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Operation> operations = operationDao.getAllDeletedOperations();
            liveData.postValue(operations);
        });
        return liveData;
    }
    
    // Восстановить операцию
    public void restoreOperation(int operationId, String updatedBy) {
        executorService.execute(() -> {
            operationDao.restoreOperation(operationId, LocalDateTime.now().toString(), updatedBy);
        });
    }
    
    // Получить общую сумму операций по типу
    public LiveData<Integer> getTotalAmountByType(String type) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = operationDao.getTotalAmountByType(type);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Получить общую сумму операций по счету
    public LiveData<Integer> getTotalAmountByAccount(int accountId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = operationDao.getTotalAmountByAccount(accountId);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Получить общую сумму операций по категории
    public LiveData<Integer> getTotalAmountByCategory(int categoryId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = operationDao.getTotalAmountByCategory(categoryId);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Получить общую сумму операций по валюте
    public LiveData<Integer> getTotalAmountByCurrency(int currencyId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = operationDao.getTotalAmountByCurrency(currencyId);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Получить количество операций
    public LiveData<Integer> getOperationsCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = operationDao.getOperationsCount();
            liveData.postValue(count);
        });
        return liveData;
    }
    
    // Получить баланс по счету
    public LiveData<Integer> getAccountBalance(int accountId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer balance = operationDao.getAccountBalance(accountId);
            liveData.postValue(balance != null ? balance : 0);
        });
        return liveData;
    }
    
    // Получить статистику по операциям
    public LiveData<OperationService.OperationStatistics> getOperationStatistics() {
        MutableLiveData<OperationService.OperationStatistics> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer totalIncome = operationDao.getTotalAmountByType("income");
            Integer totalExpense = operationDao.getTotalAmountByType("expense");
            Integer totalTransfer = operationDao.getTotalAmountByType("transfer");
            int operationsCount = operationDao.getOperationsCount();
            
            OperationService.OperationStatistics statistics = new OperationService.OperationStatistics(
                totalIncome != null ? totalIncome : 0,
                totalExpense != null ? totalExpense : 0,
                totalTransfer != null ? totalTransfer : 0,
                operationsCount
            );
            
            liveData.postValue(statistics);
        });
        return liveData;
    }
} 