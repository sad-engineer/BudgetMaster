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
    public LiveData<List<Budget>> getBudgetsByPeriod(String period) {
        return budgetRepository.getBudgetsByPeriod(period);
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
    public LiveData<List<Budget>> getBudgetsByCurrency(String currency) {
        return budgetRepository.getBudgetsByCurrency(currency);
    }
    
    // Получить общую сумму бюджетов по валюте
    public LiveData<Integer> getTotalAmountByCurrency(String currency) {
        return budgetRepository.getTotalAmountByCurrency(currency);
    }
    
    // Создать новый бюджет
    public void createBudget(String name, int amount, String currency, 
                           LocalDateTime startDate, LocalDateTime endDate, 
                           Integer categoryId, String period) {
        Budget budget = new Budget();
        budget.setName(name);
        budget.setAmount(amount);
        budget.setCurrency(currency);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setCategoryId(categoryId);
        budget.setPeriod(period);
        budget.setActive(true);
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
    public LiveData<Budget> getOrCreateBudgetByCategory(int categoryId, int amount, String currency) {
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
            newBudget.setName("Бюджет категории " + categoryId);
            newBudget.setAmount(amount);
            newBudget.setCurrency(currency);
            newBudget.setStartDate(LocalDateTime.now());
            newBudget.setEndDate(LocalDateTime.now().plusMonths(1));
            newBudget.setCategoryId(categoryId);
            newBudget.setPeriod("monthly");
            newBudget.setActive(true);
            newBudget.setPosition(1); // TODO: Получить следующую позицию
            
            budgetRepository.insertBudget(newBudget, user);
            liveData.postValue(newBudget);
        });
        return liveData;
    }
    
    // Получить или создать бюджет
    public LiveData<Budget> getOrCreateBudget(String name, int amount, String currency, 
                                            LocalDateTime startDate, LocalDateTime endDate, 
                                            Integer categoryId, String period) {
        MutableLiveData<Budget> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            // Поиск по названию
            List<Budget> budgets = budgetRepository.getAllActiveBudgets().getValue();
            if (budgets != null) {
                for (Budget budget : budgets) {
                    if (budget.getName().equals(name)) {
                        liveData.postValue(budget);
                        return;
                    }
                }
            }
            
            // Если не найден - создаем новый
            Budget newBudget = new Budget();
            newBudget.setName(name);
            newBudget.setAmount(amount);
            newBudget.setCurrency(currency);
            newBudget.setStartDate(startDate);
            newBudget.setEndDate(endDate);
            newBudget.setCategoryId(categoryId);
            newBudget.setPeriod(period);
            newBudget.setActive(true);
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
        if (budget.getName() == null || budget.getName().trim().isEmpty()) {
            return false;
        }
        if (budget.getCurrency() == null || budget.getCurrency().trim().isEmpty()) {
            return false;
        }
        if (budget.getAmount() <= 0) {
            return false;
        }
        if (budget.getStartDate() == null || budget.getEndDate() == null) {
            return false;
        }
        if (budget.getStartDate().isAfter(budget.getEndDate())) {
            return false;
        }
        if (budget.getPeriod() == null || budget.getPeriod().trim().isEmpty()) {
            return false;
        }
        return true;
    }
    
    // Проверить, активен ли бюджет
    public boolean isBudgetActive(Budget budget) {
        if (budget == null || !budget.isActive()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return !budget.getStartDate().isAfter(now) && !budget.getEndDate().isBefore(now);
    }
    
    // Получить прогресс бюджета
    public double getBudgetProgress(Budget budget, int spentAmount) {
        if (budget == null || budget.getAmount() <= 0) {
            return 0.0;
        }
        return Math.min((double)spentAmount / budget.getAmount(), 1.0);
    }
} 