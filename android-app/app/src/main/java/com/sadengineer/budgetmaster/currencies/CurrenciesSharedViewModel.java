package com.sadengineer.budgetmaster.currencies;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.base.SelectionListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared ViewModel для управления состоянием экрана валют
 */
public class CurrenciesSharedViewModel extends AndroidViewModel implements SelectionListViewModel {
    
    private static final String TAG = "CurrenciesSharedViewModel";
    
    /** Имя пользователя по умолчанию */
    private String userName = "default_user";
    
    private CurrencyService currencyService;
    
    // LiveData для управления режимом выбора и мягким удалением
    private final MutableLiveData<Boolean> selectionMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> softDeletionDone = new MutableLiveData<>();
    private final MutableLiveData<List<Currency>> selectedCurrencies = new MutableLiveData<>();
    
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    
    /**
     * Конструктор
     * @param application
     */
    public CurrenciesSharedViewModel(@NonNull Application application) {
        super(application);
        // Сервисы для работы с данными
        currencyService = new CurrencyService(application.getApplicationContext(), userName);
    }
    
    /**
     * Возвращает режим выбора
     * @return режим выбора
     */
    @Override
    public LiveData<Boolean> getSelectionMode() {
        return selectionMode;
    }
    
    /**
     * Возвращает состояние удаления
     * @return состояние удаления
     */
    @Override
    public LiveData<Boolean> getDeleting() {
        return deleting;
    }
    
    /**
     * Возвращает количество удаленных валют
     * @return количество удаленных валют
     */
    @Override
    public LiveData<Integer> getSoftDeletionDone() {
        return softDeletionDone;
    }
    
    /**
     * Возвращает выбранные валюты
     * @return выбранные валюты
     */
    public LiveData<List<Currency>> getSelectedCurrencies() {
        return selectedCurrencies;
    }
    
    /**
     * Включает режим выбора
     */
    @Override
    public void enableSelectionMode() {
        selectionMode.setValue(true);
    }
    
    /**
     * Отменяет режим выбора
     */
    @Override
    public void cancelSelectionMode() {
        selectionMode.setValue(false);
        selectedCurrencies.setValue(null);
    }
    
    /**
     * Делает softDelete для каждой валюты из переданного списка в фоне.
     */
    public void deleteCurrenciesSoft(List<Currency> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }
        
        // Фильтруем только неудаленные валюты
        List<Currency> currenciesToDelete = new ArrayList<>();
        for (Currency currency : currencies) {
            if (!currency.isDeleted()) {
                currenciesToDelete.add(currency);
            } else {
                Log.w(TAG, "Пропуск валюты: ID=" + currency.getId() + ", уже удалена");
            }
        }
        
        if (currenciesToDelete.isEmpty()) {
            Log.w(TAG, "Нет валют для удаления - все уже удалены");
            softDeletionDone.setValue(0);
            selectionMode.setValue(false);
            return;
        }
        
        Log.d(TAG, "Начато удаление валют. Количество: " + currenciesToDelete.size());
        // Устанавливаем состояние удаления
        deleting.setValue(true);
        ioExecutor.execute(() -> {
            int deletedCount = 0;
            for (Currency currency : currenciesToDelete) {
                try {
                    Log.d(TAG, "Удаление валюты: ID=" + currency.getId());
                    currencyService.delete(true, currency);
                    deletedCount++;
                    Log.d(TAG, "Валюта ID: " + currency.getId() + " успешно удалена");
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка удаления валюты: ID=" + currency.getId() + ", причина: " + e.getMessage());
                }
            }
            deleting.postValue(false);
            softDeletionDone.postValue(deletedCount);
            selectionMode.postValue(false);
            selectedCurrencies.postValue(null);
            Log.d(TAG, "Удаление завершено. Удалено валют: " + deletedCount);
        });
    }
    
    /**
     * Обновляет текущий набор выбранных валют.
     */
    public void setSelectedCurrencies(List<Currency> currencies) {
        Log.d(TAG, "Выбранных валют: " + (currencies != null ? currencies.size() : 0));
        selectedCurrencies.setValue(currencies);
    }
    
    /**
     * Делает softDelete для текущего набора выбранных валют из ViewModel.
     */
    @Override
    public void deleteSelectedItemsSoft() {
        List<Currency> currencies = selectedCurrencies.getValue();
        deleteCurrenciesSoft(currencies);
    }
}

