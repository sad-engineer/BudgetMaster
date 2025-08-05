
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.repository.BudgetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Budget
 */
public class BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final ExecutorService executorService;
    private final String user;
    
    public BudgetService(Context context, String user) {
        this.budgetRepository = new BudgetRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
    }
    
    // Получить все активные бюджеты
    public LiveData<List<Budget>> getAllActiveBudgets() {
        return budgetRepository.getAllActiveBudgets();
    }
    
    // Получить бюджеты по периоду
    public LiveData<List<Budget>> getBudgetsByPeriod() {
        return budgetRepository.getBudgetsByPeriod();
    }
    
    // Получить бюджеты по категории
    public LiveData<List<Budget>> getBudgetsByCategory(int categoryId) {
        return budgetRepository.getBudgetsByCategory(categoryId);
    }
    
    // Получить бюджет по ID
    public LiveData<Budget> getBudgetById(int id) {
        return budgetRepository.getBudgetById(id);
    }
    
    // Получить бюджеты по валюте
    public LiveData<List<Budget>> getBudgetsByCurrency(int currencyId) {
        return budgetRepository.getBudgetsByCurrency(currencyId);
    }
    
    // Получить общую сумму бюджетов по валюте
    public LiveData<Integer> getTotalAmountByCurrency(int currencyId) {
        return budgetRepository.getTotalAmountByCurrency(currencyId);
    }
    
    // Создать новый бюджет
    public void createBudget(int amount, int currencyId, Integer categoryId) {
        Budget budget = new Budget();
        budget.setAmount(amount);
        budget.setCurrencyId(currencyId);
        budget.setCategoryId(categoryId);
        budget.setPosition(1); // TODO: Получить следующую позицию
        
        budgetRepository.insertBudget(budget, user);
    }
    
    // Обновить бюджет
    public void updateBudget(Budget budget) {
        budgetRepository.updateBudget(budget, user);
    }
    
    // Удалить бюджет (soft delete)
    public void deleteBudget(int budgetId) {
        budgetRepository.deleteBudget(budgetId, user);
    }
    
    // Восстановить удаленный бюджет
    public void restoreBudget(int budgetId) {
        executorService.execute(() -> {
            // Получаем удаленный бюджет
            Budget deletedBudget = budgetRepository.getBudgetById(budgetId).getValue();
            if (deletedBudget == null || !deletedBudget.isDeleted()) {
                return; // Бюджет не найден или уже активен
            }
            
            // Очищаем поля удаления
            deletedBudget.setDeleteTime(null);
            deletedBudget.setDeletedBy(null);
            deletedBudget.setUpdateTime(LocalDateTime.now());
            deletedBudget.setUpdatedBy(user);
            
            // Обновляем бюджет в базе
            budgetRepository.updateBudget(deletedBudget, user);
        });
    }
    
    // Изменить позицию бюджета (сложная логика)
    public void changePosition(Budget budget, int newPosition) {
        executorService.execute(() -> {
            int oldPosition = budget.getPosition();
            
            // Если позиция не изменилась, ничего не делаем
            if (oldPosition == newPosition) {
                return;
            }
            
            // Получаем все активные бюджеты для переупорядочивания
            List<Budget> allBudgets = budgetRepository.getAllActiveBudgets().getValue();
            if (allBudgets == null) return;
            
            // Проверяем, что новая позиция валидна
            int maxPosition = allBudgets.size();
            if (newPosition < 1 || newPosition > maxPosition) {
                throw new IllegalArgumentException("Позиция вне диапазона: " + maxPosition);
            }
            
            // Переупорядочиваем позиции
            if (oldPosition < newPosition) {
                // Двигаем бюджет вниз: сдвигаем бюджеты между старой и новой позицией вверх
                for (Budget b : allBudgets) {
                    if (b.getId() != budget.getId() && 
                        b.getPosition() > oldPosition && 
                        b.getPosition() <= newPosition) {
                        b.setPosition(b.getPosition() - 1);
                        budgetRepository.updateBudget(b, user);
                    }
                }
            } else {
                // Двигаем бюджет вверх: сдвигаем бюджеты между новой и старой позицией вниз
                for (Budget b : allBudgets) {
                    if (b.getId() != budget.getId() && 
                        b.getPosition() >= newPosition && 
                        b.getPosition() < oldPosition) {
                        b.setPosition(b.getPosition() + 1);
                        budgetRepository.updateBudget(b, user);
                    }
                }
            }
            
            // Устанавливаем новую позицию для текущего бюджета
            budget.setPosition(newPosition);
            budgetRepository.updateBudget(budget, user);
        });
    }
    
    // Получить бюджет по ID категории
    public LiveData<Budget> getBudgetByCategoryId(int categoryId) {
        return budgetRepository.getBudgetByCategoryId(categoryId);
    }
    
    // Получить или создать бюджет по категории
    public LiveData<Budget> getOrCreateBudgetByCategory(int categoryId, int amount, int currencyId) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по ID категории
            Budget existingBudget = budgetRepository.getBudgetByCategoryId(categoryId).getValue();
            if (existingBudget != null) {
                liveData.postValue(existingBudget);
                return;
            }
            
            // Если не найден - создаем новый
            Budget newBudget = new Budget();
            newBudget.setAmount(amount);
            newBudget.setCurrencyId(currencyId);
            newBudget.setCategoryId(categoryId);
            newBudget.setPosition(1); // TODO: Получить следующую позицию
            
            budgetRepository.insertBudget(newBudget, user);
            liveData.postValue(newBudget);
        });
        return liveData;
    }
    
    // Получить или создать бюджет
    public LiveData<Budget> getOrCreateBudget(int amount, int currencyId, Integer categoryId) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по параметрам
            List<Budget> budgets = budgetRepository.getAllActiveBudgets().getValue();
            if (budgets != null) {
                for (Budget budget : budgets) {
                    if (budget.getAmount() == amount && 
                        budget.getCurrencyId() == currencyId && 
                        budget.getCategoryId() == categoryId) {
                        liveData.postValue(budget);
                        return;
                    }
                }
            }
            
            // Если не найден - создаем новый
            Budget newBudget = new Budget();
            newBudget.setAmount(amount);
            newBudget.setCurrencyId(currencyId);
            newBudget.setCategoryId(categoryId);
            newBudget.setPosition(1); // TODO: Получить следующую позицию
            
            budgetRepository.insertBudget(newBudget, user);
            liveData.postValue(newBudget);
        });
        return liveData;
    }
    
    // Получить количество активных бюджетов
    public LiveData<Integer> getActiveBudgetsCount() {
        return budgetRepository.getActiveBudgetsCount();
    }
    
    // Валидация бюджета
    public boolean validateBudget(Budget budget) {
        if (budget.getCurrencyId() <= 0) {
            return false;
        }
        if (budget.getAmount() <= 0) {
            return false;
        }
        return true;
    }
    
    // Проверить, активен ли бюджет
    public boolean isBudgetActive(Budget budget) {
        if (budget == null || budget.isDeleted()) {
            return false;
        }
        
        return true;
    }
    
    // Получить прогресс бюджета
    public double getBudgetProgress(Budget budget, int spentAmount) {
        if (budget == null || budget.getAmount() <= 0) {
            return 0.0;
        }
        return Math.min((double)spentAmount / budget.getAmount(), 1.0);
    }
} 