package com.sadengineer.budgetmaster.accounts.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sadengineer.budgetmaster.utils.LogManager;

import java.io.Serializable;
import java.util.List;

/**
 * Базовый ViewModel для работы со списками
 * Содержит только общую функциональность
 * 
 * @param <T> Тип элементов списка
 */
public abstract class ListViewModel<T extends Serializable> extends ViewModel {
    
    // Тег для логирования
    protected final String TAG = this.getClass().getSimpleName();
    
    // Общие LiveData для всех списков
    protected final MutableLiveData<List<T>> items = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Публичные методы для UI
    
    /**
     * Возвращает список элементов
     */
    public LiveData<List<T>> getItems() {
        return items;
    }
    
    /**
     * Возвращает состояние загрузки
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Возвращает сообщение об ошибке
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Обновляет список элементов
     */
    public void setItems(List<T> newItems) {
        items.setValue(newItems);
        LogManager.d(TAG, "Список обновлен. Количество элементов: " + (newItems != null ? newItems.size() : 0));
    }
    
    /**
     * Устанавливает состояние загрузки
     */
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
        LogManager.d(TAG, "Состояние загрузки: " + loading);
    }
    
    /**
     * Устанавливает сообщение об ошибке
     */
    public void setError(String message) {
        errorMessage.setValue(message);
        LogManager.e(TAG, "Ошибка: " + message);
    }
    
    /**
     * Очищает ошибку
     */
    public void clearError() {
        errorMessage.setValue(null);
        LogManager.d(TAG, "Ошибка очищена");
    }
    
    /**
     * Обновляет данные
     */
    public void refreshData() {
        LogManager.d(TAG, "Обновление данных");
        setLoading(true);
        clearError();
        
        try {
            loadItems();
        } catch (Exception e) {
            handleError("Ошибка загрузки данных", e);
        }
    }
    
    /**
     * Обрабатывает ошибки
     */
    protected void handleError(String message, Throwable throwable) {
        LogManager.e(TAG, message, throwable);
        setError(message);
        setLoading(false);
    }
    
    /**
     * Обрабатывает успешную загрузку данных
     */
    protected void handleDataLoaded(List<T> loadedItems) {
        LogManager.d(TAG, "Данные загружены. Количество: " + (loadedItems != null ? loadedItems.size() : 0));
        setItems(loadedItems);
        setLoading(false);
        clearError();
    }
    
    // Абстрактные методы для реализации в наследниках
    
    /**
     * Загружает данные
     * Должен быть реализован в наследниках
     */
    protected abstract void loadItems();
    
    /**
     * Добавляет элемент в список
     * @param item элемент для добавления
     */
    public abstract void addItem(T item);
    
    /**
     * Обновляет элемент в списке
     * @param item элемент для обновления
     */
    public abstract void updateItem(T item);
    
    /**
     * Удаляет элемент из списка
     * @param item элемент для удаления
     */
    public abstract void removeItem(T item);
}
