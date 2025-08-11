
package com.sadengineer.budgetmaster.backend.service;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.EntityFilter;
import com.sadengineer.budgetmaster.backend.repository.BudgetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service класс для бизнес-логики работы с Budget
 */
public class BudgetService {
    private static final String TAG = "BudgetService";
    
    private final BudgetRepository repo;
    private final ExecutorService executorService;
    private final String user;
    
    public BudgetService(Context context, String user) {
        this.repo = new BudgetRepository(context);
        this.executorService = Executors.newFixedThreadPool(4);
        this.user = user;
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
    public void create(int category_id, Integer amount, Integer currency_id) {
        if (category_id <= 0) {
            throw new IllegalArgumentException("ID категории не может быть пустым");
        }

        executorService.execute(() -> {
            try {
                // Создаем переменные ВНУТРИ lambda
                int finalAmount = amount;
                int finalCurrencyId = currency_id;
                
                // Проверяем, если сумма не передана, устанавливаем значение по умолчанию
                if (amount == null || amount <= 0) {
                    Log.d(TAG, "Для категории " + category_id + " не передано значение суммы, устанавливаем значение по умолчанию");
                    finalAmount = 0;
                }
                
                // Проверяем, если ID валюты не передан, устанавливаем значение по умолчанию
                if (currency_id == null || currency_id <= 0) {
                    Log.d(TAG, "Для категории " + category_id + " не передано значение ID валюты, устанавливаем значение по умолчанию");
                    finalCurrencyId = 1;
                }
                
                createBudgetInTransaction(category_id, finalAmount, finalCurrencyId);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при создании бюджета для категории " + category_id + ": " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для создания нового бюджета
     * @param category_id ID категории
     * @param amount сумма
     * @param currency_id ID валюты
     */
    @Transaction
    private void createBudgetInTransaction(int category_id, Integer amount, Integer currency_id) {
        Log.d(TAG, "🔄 Запрос на создание бюджета для категории " + category_id);
        Budget budget = new Budget();
        budget.setCategoryId(category_id);
        budget.setAmount(amount);
        budget.setCurrencyId(currency_id);
        budget.setPosition(repo.getMaxPosition() + 1);
        budget.setCreateTime(LocalDateTime.now());
        budget.setCreatedBy(user);
        repo.insert(budget);
        Log.d(TAG, "✅ Бюджет для категории " + budget.getCategoryId() + " успешно создан");
    }

    /**
     * Удалить бюджет (полное удаление - удаление строки из БД)
     * @param budget бюджет
     */
    private void delete(Budget budget) {
        if (budget == null) {
            Log.e(TAG, "❌ Бюджет не найден для удаления. Удаление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                deleteBudgetInTransaction(budget);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при удалении бюджета: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для удаления бюджета
     * @param budget бюджет
     */
    @Transaction
    private void deleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, "🔄 Запрос на удаление бюджета для категории " + budget.getCategoryId());
        int deletedPosition = budget.getPosition();
        repo.delete(budget);
        Log.d(TAG, "✅ Бюджет для категории " + budget.getCategoryId() + " успешно удален");
    }

    /**
     * Удалить бюджет (полное удаление - удаление строки из БД)
     * @param softDelete true - soft delete, false - полное удаление
     * @param budget бюджет
     */
    public void delete(boolean softDelete, Budget budget) {
        if (softDelete) {
            softDelete(budget);
        } else {
            delete(budget);
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
        return repo.getAll();
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
     * Восстановить удаленный бюджет (soft delete)
     * @param deletedBudget удаленный бюджет
     */
    public void restore(Budget deletedBudget) {
        executorService.execute(() -> {
            try {
                restoreBudgetInTransaction(deletedBudget);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при восстановлении бюджета: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для восстановления бюджета
     * @param deletedBudget удаленный бюджет
     */
    @Transaction
    private void restoreBudgetInTransaction(Budget deletedBudget) {
        if (deletedBudget != null) {
            Log.d(TAG, "🔄 Запрос на восстановление бюджета для категории " + deletedBudget.getCategoryId());
            deletedBudget.setPosition(repo.getMaxPosition() + 1);
            deletedBudget.setDeleteTime(null);
            deletedBudget.setDeletedBy(null);
            deletedBudget.setUpdateTime(LocalDateTime.now());
            deletedBudget.setUpdatedBy(user);
            repo.update(deletedBudget);
            Log.d(TAG, "✅ Бюджет для категории " + deletedBudget.getCategoryId() + " успешно восстановлен");
        } else {
            Log.e(TAG, "❌ Бюджет не найден для восстановления");
        }
    }

    /**
     * Удалить бюджет (soft delete)
     * @param budget бюджет
     */
    private void softDelete(Budget budget) {
        if (budget == null) {
            Log.e(TAG, "❌ Бюджет не найден для soft delete. Удаление было отменено");
            return;
        }   

        executorService.execute(() -> {
            try {
                softDeleteBudgetInTransaction(budget);
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при soft delete бюджета: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Транзакция для удаления бюджета (soft delete)
     * @param budget бюджет
     */
    @Transaction
    private void softDeleteBudgetInTransaction(Budget budget) {
        Log.d(TAG, "🔄 Запрос на softDelete бюджета для категории " + budget.getCategoryId());
        int deletedPosition = budget.getPosition();
        budget.setPosition(0);
        budget.setDeleteTime(LocalDateTime.now());
        budget.setDeletedBy(user);
        repo.update(budget);
        // Пересчитываем позиции после soft delete
        repo.shiftPositionsDown(deletedPosition);
        Log.d(TAG, "✅ Бюджет для категории " + budget.getCategoryId() + " успешно soft deleted");
    }

    /**
     * Обновить бюджет
     * @param budget бюджет
     */
    public void update(Budget budget) {
        if (budget == null) {
            Log.e(TAG, "❌ Бюджет не найден для обновления. Обновление было отменено");
            return;
        }

        executorService.execute(() -> {
            try {
                Log.d(TAG, "🔄 Запрос на обновление бюджета для категории " + budget.getCategoryId());
                budget.setUpdateTime(LocalDateTime.now());
                budget.setUpdatedBy(user);
                repo.update(budget);
                Log.d(TAG, "✅ Запрос на обновление бюджета для категории " + budget.getCategoryId() + " успешно отправлен");
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка при обновлении бюджета для категории " + budget.getCategoryId() + ": " + e.getMessage(), e);
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
        return repo.getCount();
    }
    
    /**
     * Закрыть ExecutorService
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 