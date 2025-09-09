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
import com.sadengineer.budgetmaster.backend.constants.ServiceConstants;
import com.sadengineer.budgetmaster.backend.interfaces.IService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Service класс для бизнес-логики работы с Budget
 */
public class BudgetService implements IService<Budget> {
    private static final String TAG = "BudgetService";
    
    private final BudgetRepository repo;
    private final ExecutorService executorService;
    private final String user;
    public final BudgetValidator validator;
    private final CategoryRepository categoryRepo;
    private final CurrencyRepository currencyRepo;
    private final ServiceConstants constants;

    public BudgetService(Context context, String user) {
        this.repo = new BudgetRepository(context);
        this.executorService = ThreadManager.getExecutor();
        this.user = user;
        this.validator = new BudgetValidator();
        this.categoryRepo = new CategoryRepository(context);
        this.currencyRepo = new CurrencyRepository(context);
        this.constants = new ServiceConstants();
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
    public void createBudgetInTransaction(int category_id, Long amount, int currency_id) {
        Log.d(TAG, String.format(constants.MSG_CREATE_BUDGET_REQUEST, category_id));
        Budget budget = new Budget();
        budget.setCategoryId(category_id);
        budget.setAmount(amount);
        budget.setCurrencyId(currency_id);
        budget.setPosition(repo.getMaxPosition() + 1);
        budget.setCreateTime(LocalDateTime.now());
        budget.setCreatedBy(user);
        try {
            repo.insert(budget);
            Log.d(TAG, String.format(constants.MSG_BUDGET_CREATED, budget.getCategoryId()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_CREATE_BUDGET_ERROR, category_id) + e.getMessage(), e);
        }
    }

    /**
     * Создать новый бюджет без проверок значений
     * @param category_id ID категории
     */
    public void createWithoutValidation(int category_id, long amount, int currency_id) {
        executorService.execute(() -> {
            createBudgetInTransaction(category_id, amount, currency_id);
        });
    }

    /**
     * Удалить бюджет (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param budget бюджет
     */
    public void delete(Budget budget, boolean softDelete) {
        if (budget == null) {
            Log.e(TAG, constants.MSG_DELETE_BUDGET_NOT_FOUND);
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
    public void deleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, String.format(constants.MSG_DELETE_BUDGET_REQUEST, budget.getCategoryId()));
        try {
            repo.delete(budget);
            Log.d(TAG, String.format(constants.MSG_BUDGET_DELETED, budget.getCategoryId()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_DELETE_BUDGET_ERROR, budget.getCategoryId()) + e.getMessage(), e);
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
     * Получить бюджет по категории синхронно (для транзакций)
     * @param category_id ID категории
     * @return бюджет или null
     */
    public Budget getByCategorySync(int category_id) {
        return repo.getByCategorySync(category_id);
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
            Log.e(TAG, constants.MSG_RESTORE_BUDGET_NOT_FOUND);
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
        Log.d(TAG, String.format(constants.MSG_RESTORE_BUDGET_REQUEST, deletedBudget.getCategoryId()));
        deletedBudget.setPosition(repo.getMaxPosition() + 1);
        deletedBudget.setDeleteTime(null);
        deletedBudget.setDeletedBy(null);
        deletedBudget.setUpdateTime(LocalDateTime.now());
        deletedBudget.setUpdatedBy(user);
        try {
            repo.update(deletedBudget);
            Log.d(TAG, String.format(constants.MSG_BUDGET_RESTORED, deletedBudget.getCategoryId()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_RESTORE_BUDGET_ERROR, deletedBudget.getCategoryId()) + e.getMessage(), e);
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
    public void softDeleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, String.format(constants.MSG_SOFT_DELETE_BUDGET_REQUEST, budget.getCategoryId()));
        int deletedPosition = budget.getPosition();
        budget.setPosition(0);
        budget.setDeleteTime(LocalDateTime.now());
        budget.setDeletedBy(user);
        try {   
            repo.update(budget);
            // Пересчитываем позиции после soft delete
            repo.shiftPositionsDown(deletedPosition);
            Log.d(TAG, String.format(constants.MSG_BUDGET_SOFT_DELETED, budget.getCategoryId()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_SOFT_DELETE_BUDGET_ERROR, budget.getCategoryId()) + ": " + e.getMessage(), e);
        }
    }

    /**
     * Обновить бюджет
     * @param budget бюджет
     */
    public void update(Budget budget) {
        if (budget == null) {
            Log.e(TAG, constants.MSG_UPDATE_BUDGET_NOT_FOUND);
            return;
        }

        executorService.execute(() -> {
            updateBudgetInTransaction(budget);
        });
    }

    /**
     * Транзакция для обновления бюджета
     * @param budget бюджет
     */
    @Transaction
    public void updateBudgetInTransaction(Budget budget) {
        try {
            Log.d(TAG, String.format(constants.MSG_UPDATE_BUDGET_REQUEST, budget.getCategoryId()));
            budget.setUpdateTime(LocalDateTime.now());
            budget.setUpdatedBy(user);
            repo.update(budget);
            Log.d(TAG, String.format(constants.MSG_BUDGET_UPDATED, budget.getCategoryId()));
        } catch (Exception e) {
            Log.e(TAG, String.format(constants.MSG_UPDATE_BUDGET_ERROR, budget.getCategoryId()) + ": " + e.getMessage(), e);
        }
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
     * Получить общую сумму бюджета по ID валюты по фильтру
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return общая сумма бюджета по ID валюты
     */
    public LiveData<Long> getTotalAmountByCurrency(int currencyId, EntityFilter filter) {
        return repo.getTotalAmountByCurrency(currencyId, filter);
    }

    /**
     * Получить все бюджеты по ID валюты (синхронно)
     * @param currencyId ID валюты
     * @param filter фильтр для выборки бюджетов (ACTIVE, DELETED, ALL)
     * @return список бюджетов
     */
    public List<Budget> getAllByCurrencySync(int currencyId, EntityFilter filter) {
        return repo.getAllByCurrencySync(currencyId, filter);
    }

} 