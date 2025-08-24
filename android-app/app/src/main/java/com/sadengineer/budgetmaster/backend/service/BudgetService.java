package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.backend.repository.BudgetRepository;
import com.sadengineer.budgetmaster.backend.repository.CategoryRepository;
import com.sadengineer.budgetmaster.backend.repository.CurrencyRepository;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.validator.BudgetValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Budget
 */
public class BudgetService {
    private static final String TAG = "BudgetService";
    
    private final BudgetRepository repo;
    private final ExecutorService executorService;
    private final String user;
    private final BudgetValidator validator;
    private final CategoryRepository categoryRepo;
    private final CurrencyRepository currencyRepo;

    public BudgetService(Context context, String user) {
        this.repo = new BudgetRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.validator = new BudgetValidator();
        this.categoryRepo = new CategoryRepository(context);
        this.currencyRepo = new CurrencyRepository(context);
    }

    /**
     * Изменить позицию бюджета
     * @param budget бюджет
     * @param newPosition новая позиция
     */
    public void changePosition(Budget budget, int newPosition) {
        executorService.execute(() -> {
            changePositionInTransaction(budget, newPosition);
        });
    }
    
    /**
     * Транзакция для изменения позиции бюджета
     * @param budget бюджет
     * @param newPosition новая позиция
     */
    @Transaction
    private void changePositionInTransaction(Budget budget, int newPosition) {
        int oldPosition = budget.getPosition();
        
        // Если позиция не изменилась, ничего не делаем
        if (oldPosition == newPosition) {
            return;
        }
        
        // Используем методы сдвига позиций из Repository
        if (oldPosition < newPosition) {
            repo.shiftPositionsDown(oldPosition);
            repo.shiftPositionsUp(newPosition + 1);
        } else {
            repo.shiftPositionsUp(newPosition);
            repo.shiftPositionsDown(oldPosition);
        }
        
        // Устанавливаем новую позицию для текущего бюджета
        budget.setPosition(newPosition);
        repo.update(budget);
    }
    
    /**
     * Изменить позицию бюджета по старой позиции
     * @param oldPosition старая позиция
     * @param newPosition новая позиция
     */
    public void changePosition(int oldPosition, int newPosition) {
        Budget budget = repo.getByPosition(oldPosition).getValue();
        if (budget != null) {
            changePosition(budget, newPosition);
        }
    }
    
    /**
     * Изменить позицию бюджета по названию
     * @param category_id ID категории  
     * @param newPosition новая позиция
     */
    // public void changePosition(int category_id, int newPosition) {
    //     Budget budget = repo.getByCategory(category_id).getValue();
    //     if (budget != null) {
    //         changePosition(budget, newPosition);
    //     }
    // }

    /**
     * Создать новый бюджет
     * @param category_id ID категории (обязательный параметр, не может быть пустым)
     * @param amount сумма (необязательный параметр, перелайте null, для установки значения по умолчанию (0))
     * @param currency_id ID валюты (необязательный параметр, перелайте null, для установки значения по умолчанию (1))
     */
    public void create(Integer category_id, Long amount, Integer currency_id) {
        validator.validateCategoryId(category_id, categoryRepo.getCount(EntityFilter.ALL));
        validator.validateAmount(amount);
        validator.validateCurrencyId(currency_id, currencyRepo.getCount(EntityFilter.ALL));

        executorService.execute(() -> {
            createBudgetInTransaction(category_id, amount, currency_id);
        });
    }
    
    /**
     * Транзакция для создания нового бюджета
     * @param category_id ID категории
     * @param amount сумма
     * @param currency_id ID валюты
     */
    @Transaction
    private void createBudgetInTransaction(int category_id, Long amount, int currency_id) {
        Log.d(TAG, "Запрос на создание бюджета для категории " + category_id);
        Budget budget = new Budget();
        budget.setCategoryId(category_id);
        budget.setAmount(amount);
        budget.setCurrencyId(currency_id);
        budget.setPosition(repo.getMaxPosition() + 1);
        budget.setCreateTime(LocalDateTime.now());
        budget.setCreatedBy(user);
        try {
            repo.insert(budget);
            Log.d(TAG, "Бюджет для категории " + budget.getCategoryId() + " успешно создан");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при создании бюджета для категории " + category_id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Удалить бюджет (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param budget бюджет
     */
    public void delete(Budget budget, boolean softDelete) {
        if (budget == null) {
            Log.e(TAG, "Бюджет не найден для удаления. Удаление было отменено");
            return;
        }
        if (softDelete) {
            softDelete(budget);
        } else {
            delete(budget);
        }
    }

    /**
     * Удалить бюджет (полное удаление - удаление строки из БД)
     * @param budget бюджет
     */
    private void delete(Budget budget) {
        executorService.execute(() -> {
            deleteBudgetInTransaction(budget);
        });
    }
    
    /**
     * Транзакция для удаления бюджета
     * @param budget бюджет
     */
    @Transaction
    private void deleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, "Запрос на удаление бюджета для категории " + budget.getCategoryId());
        try {
            repo.delete(budget);
            Log.d(TAG, "Бюджет для категории " + budget.getCategoryId() + " успешно удален");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении бюджета для категории " + budget.getCategoryId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Получить все бюджеты
     * @param filter фильтр для выборки бюджетов
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAll(EntityFilter filter) {
        return repo.getAll(filter);
    }
    
    /**
     * Получить все бюджеты (включая удаленные)
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAll() {
        return repo.getAll(EntityFilter.ALL);
    }

    /**
     * Получить все бюджеты по типу операции
     * @param operationType фильтр типа операции
     * @param filter фильтр для выборки бюджетов
     * @return LiveData со списком всех бюджетов
     */
    public LiveData<List<Budget>> getAllByOperationType(OperationTypeFilter operationType, EntityFilter filter) {
        return repo.getAllByOperationType(operationType, filter);
    }

    /**
     * Получить бюджет по ID категории
     * @param category_id ID категории
     * @return LiveData с бюджетом
     */
    public LiveData<Budget> getByCategory(int category_id) {
        return repo.getByCategory(category_id);
    }

    /**
     * Получить бюджет по ID
     * @param id ID бюджета
     * @return LiveData с бюджетом
     */
    public LiveData<Budget> getById(int id) {
        return repo.getById(id);
    }

    /**
     * Восстановить удаленный бюджет (soft delete)
     * @param deletedBudget удаленный бюджет
     */
    public void restore(Budget deletedBudget) {
        if (deletedBudget == null) {
            Log.e(TAG, "Бюджет не найден для восстановления");
            return;
        }
        executorService.execute(() -> {
            restoreBudgetInTransaction(deletedBudget);
        });
    }
    
    /**
     * Транзакция для восстановления бюджета
     * @param deletedBudget удаленный бюджет
     */
    @Transaction
    private void restoreBudgetInTransaction(Budget deletedBudget) {
        Log.d(TAG, "Запрос на восстановление бюджета для категории " + deletedBudget.getCategoryId());
        deletedBudget.setPosition(repo.getMaxPosition() + 1);
        deletedBudget.setDeleteTime(null);
        deletedBudget.setDeletedBy(null);
        deletedBudget.setUpdateTime(LocalDateTime.now());
        deletedBudget.setUpdatedBy(user);
        try {
            repo.update(deletedBudget);
            Log.d(TAG, "Бюджет для категории " + deletedBudget.getCategoryId() + " успешно восстановлен");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при восстановлении бюджета для категории " + deletedBudget.getCategoryId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Удалить бюджет (soft delete)
     * @param budget бюджет
     */
    private void softDelete(Budget budget) {
        executorService.execute(() -> {
            softDeleteBudgetInTransaction(budget);
        });
    }
    
    /**
     * Транзакция для удаления бюджета (soft delete)
     * @param budget бюджет
     */
    @Transaction
    private void softDeleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, "Запрос на softDelete бюджета для категории " + budget.getCategoryId());
        int deletedPosition = budget.getPosition();
        budget.setPosition(0);
        budget.setDeleteTime(LocalDateTime.now());
        budget.setDeletedBy(user);
        try {   
            repo.update(budget);
            // Пересчитываем позиции после soft delete
            repo.shiftPositionsDown(deletedPosition);
            Log.d(TAG, "Бюджет для категории " + budget.getCategoryId() + " успешно soft deleted");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при soft delete бюджета для категории " + budget.getCategoryId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обновить бюджет
     * @param budget бюджет
     */
    public void update(Budget budget) {
        if (budget == null) {
            Log.e(TAG, "Бюджет не найден для обновления. Обновление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, "Запрос на обновление бюджета для категории " + budget.getCategoryId());
                budget.setUpdateTime(LocalDateTime.now());
                budget.setUpdatedBy(user);
                repo.update(budget);
                Log.d(TAG, "Запрос на обновление бюджета для категории " + budget.getCategoryId() + " успешно отправлен");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении бюджета для категории " + budget.getCategoryId() + ": " + e.getMessage(), e);
            }
        });
    }

    /**
     * Получить количество бюджетов
     * @param filter фильтр для выборки бюджетов
     * @return количество бюджетов
     */
    public int getCount(EntityFilter filter) {
        return repo.getCount(filter);
    }
    
    /**
     * Получить общее количество бюджетов (включая удаленные)
     * @return общее количество бюджетов
     */
    public int getCount() {
        return repo.getCount(EntityFilter.ALL);
    }
    
    /**
     * Закрыть ExecutorService
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 