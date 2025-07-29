// -*- coding: utf-8 -*-
package com.sadengineer.budgetmaster.backend.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.dao.BudgetDao;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import com.sadengineer.budgetmaster.backend.entity.Budget;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository класс для работы с Budget Entity
 */
public class BudgetRepository {
    
    private final BudgetDao budgetDao;
    private final ExecutorService executorService;
    
    public BudgetRepository(Context context) {
        BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(context);
        this.budgetDao = database.budgetDao();
        this.executorService = Executors.newFixedThreadPool(4);
    }
    
    // Получить все активные бюджеты
    public LiveData<List<Budget>> getAllActiveBudgets() {
        MutableLiveData<List<Budget>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Budget> budgets = budgetDao.getAllActiveBudgets();
            liveData.postValue(budgets);
        });
        return liveData;
    }
    
    // Получить бюджеты по периоду
    public LiveData<List<Budget>> getBudgetsByPeriod() {
        MutableLiveData<List<Budget>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Budget> budgets = budgetDao.getBudgetsByPeriod();
            liveData.postValue(budgets);
        });
        return liveData;
    }
    
    // Получить бюджеты по категории
    public LiveData<List<Budget>> getBudgetsByCategory(int categoryId) {
        MutableLiveData<List<Budget>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Budget> budgets = budgetDao.getBudgetsByCategory(categoryId);
            liveData.postValue(budgets);
        });
        return liveData;
    }
    
    // Получить бюджет по ID
    public LiveData<Budget> getBudgetById(int id) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Budget budget = budgetDao.getBudgetById(id);
            liveData.postValue(budget);
        });
        return liveData;
    }
    
    // Получить бюджет по ID категории
    public LiveData<Budget> getBudgetByCategoryId(int categoryId) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Budget budget = budgetDao.getBudgetByCategoryId(categoryId);
            liveData.postValue(budget);
        });
        return liveData;
    }
    
    // Получить бюджеты по валюте
    public LiveData<List<Budget>> getBudgetsByCurrency(int currencyId) {
        MutableLiveData<List<Budget>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Budget> budgets = budgetDao.getBudgetsByCurrency(currencyId);
            liveData.postValue(budgets);
        });
        return liveData;
    }
    
    // Получить общую сумму бюджетов по валюте
    public LiveData<Integer> getTotalAmountByCurrency(int currencyId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer total = budgetDao.getTotalAmountByCurrency(currencyId);
            liveData.postValue(total != null ? total : 0);
        });
        return liveData;
    }
    
    // Вставить новый бюджет
    public void insertBudget(Budget budget, String createdBy) {
        executorService.execute(() -> {
            budget.setCreateTime(LocalDateTime.now());
            budget.setCreatedBy(createdBy);
            budget.setUpdateTime(LocalDateTime.now());
            budget.setUpdatedBy(createdBy);
            budgetDao.insertBudget(budget);
        });
    }
    
    // Обновить бюджет
    public void updateBudget(Budget budget, String updatedBy) {
        executorService.execute(() -> {
            budget.setUpdateTime(LocalDateTime.now());
            budget.setUpdatedBy(updatedBy);
            budgetDao.updateBudget(budget);
        });
    }
    
    // Удалить бюджет (soft delete)
    public void deleteBudget(int budgetId, String deletedBy) {
        executorService.execute(() -> {
            budgetDao.softDeleteBudget(budgetId, LocalDateTime.now().toString(), deletedBy);
        });
    }
    
    // Получить все удаленные бюджеты
    public LiveData<List<Budget>> getAllDeletedBudgets() {
        MutableLiveData<List<Budget>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Budget> budgets = budgetDao.getAllDeletedBudgets();
            liveData.postValue(budgets);
        });
        return liveData;
    }
    

    
    // Получить максимальную позицию
    public LiveData<Integer> getMaxPosition() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Integer maxPos = budgetDao.getMaxPosition();
            liveData.postValue(maxPos != null ? maxPos : 0);
        });
        return liveData;
    }
    
    // Восстановить бюджет
    public void restoreBudget(int budgetId, String updatedBy) {
        executorService.execute(() -> {
            budgetDao.restoreBudget(budgetId, LocalDateTime.now().toString(), updatedBy);
        });
    }
    
    // Получить бюджет по позиции
    public LiveData<Budget> getBudgetByPosition(int position) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Budget budget = budgetDao.getBudgetByPosition(position);
            liveData.postValue(budget);
        });
        return liveData;
    }
    
    // Получить количество активных бюджетов
    public LiveData<Integer> getActiveBudgetsCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            int count = budgetDao.getActiveBudgetsCount();
            liveData.postValue(count);
        });
        return liveData;
    }
} 