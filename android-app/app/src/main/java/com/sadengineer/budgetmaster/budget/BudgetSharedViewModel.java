package com.sadengineer.budgetmaster.budget;

import androidx.lifecycle.ViewModel;

/**
 * ViewModel для работы с бюджетами
 * Содержит общую логику и состояние для бюджетных фрагментов
 */
public class BudgetSharedViewModel extends ViewModel {
    
    private static final String TAG = "BudgetSharedViewModel";
    
    // Состояние выбора элементов
    private boolean isSelectionMode = false;
    
    /**
     * Проверить, включен ли режим выбора
     * @return true если режим выбора активен
     */
    public boolean isSelectionMode() {
        return isSelectionMode;
    }
    
    /**
     * Включить/выключить режим выбора
     * @param selectionMode true для включения режима выбора
     */
    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
    }
    
    /**
     * Переключить режим выбора
     */
    public void toggleSelectionMode() {
        this.isSelectionMode = !this.isSelectionMode;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Сброс состояния при очистке ViewModel
        isSelectionMode = false;
    }
}
