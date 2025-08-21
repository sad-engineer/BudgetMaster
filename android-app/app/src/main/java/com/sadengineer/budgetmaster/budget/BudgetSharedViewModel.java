package com.sadengineer.budgetmaster.budget;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.service.BudgetService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel для управления режимом выбора и мягким удалением бюджетов
 */
public class BudgetSharedViewModel extends ViewModel {
    
    private static final String TAG = "BudgetSharedViewModel";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> softDeletionDone = new MutableLiveData<>();
    
    /**
     * Получает режим выбора
     */
    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }
    
    /**
     * Устанавливает режим выбора
     */
    public void setSelectionMode(boolean enabled) {
        selectionMode.setValue(enabled);
        Log.d(TAG, "Режим выбора: " + (enabled ? "включен" : "выключен"));
    }
    
    /**
     * Получает результат мягкого удаления
     */
    public LiveData<Integer> getSoftDeletionDone() {
        return softDeletionDone;
    }
    
    /**
     * Выполняет мягкое удаление выбранных бюджетов
     */
    public void softDeleteSelectedBudgets(List<Budget> selectedBudgets) {
        if (selectedBudgets == null || selectedBudgets.isEmpty()) {
            Log.w(TAG, "Нет выбранных бюджетов для удаления");
            return;
        }
        
        Log.d(TAG, "Начинаем мягкое удаление " + selectedBudgets.size() + " бюджетов");
        
        executor.execute(() -> {
            try {
                BudgetService budgetService = new BudgetService(null, userName);
                
                int deletedCount = 0;
                for (Budget budget : selectedBudgets) {
                    try {
                        budgetService.delete(true, budget); // true = softDelete
                        deletedCount++;
                        Log.d(TAG, "Бюджет мягко удален: " + budget.getId());
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка мягкого удаления бюджета: " + budget.getId() + ": " + e.getMessage());
                    }
                }
                
                // Уведомляем о результате
                softDeletionDone.postValue(deletedCount);
                
                // Выключаем режим выбора
                selectionMode.postValue(false);
                
                Log.d(TAG, "Мягкое удаление завершено. Удалено бюджетов: " + deletedCount);
                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при мягком удалении бюджетов: " + e.getMessage(), e);
                softDeletionDone.postValue(0);
                selectionMode.postValue(false);
            }
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "BudgetSharedViewModel очищен");
    }
}
