package com.sadengineer.budgetmaster.calculators;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;
import java.util.Map;

/**
 * ViewModel для расчета общих сумм счетов по валютам и типам
 * Автоматически отслеживает изменения в базе данных и пересчитывает общую сумму
 */
public class AccountCalculatorViewModel extends BasicCalculatorForCurrencyItems {

    private static final String TAG = "AccountCalculatorViewModel";

    /** Сервис для работы со счетами */
    private final AccountService accountService;
    
    /** Фильтр типа счетов */
    private final AccountTypeFilter accountTypeFilter;
    
    /** Счетчики для отслеживания загрузки валют */
    private int loadedCurrenciesCount = 0;
    private int totalCurrenciesCount = 0;

    /**
     * Конструктор
     * @param application контекст приложения
     * @param accountTypeFilter фильтр типа счетов
     */
    public AccountCalculatorViewModel(@NonNull Application application, @NonNull AccountTypeFilter accountTypeFilter) {
        super(application);
        
        // Инициализируем сервис счетов
        accountService = new AccountService(application, "AccountCalculator");
        this.accountTypeFilter = accountTypeFilter;
        
        LogManager.d(TAG, "AccountCalculatorViewModel создан с фильтром: " + accountTypeFilter);
    }

    @Override
    protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
        LogManager.d(TAG, "Обновление сумм счетов для " + newCurrencyIds.size() + " валют, тип: " + accountTypeFilter);
        
        // Сбрасываем счетчики
        loadedCurrenciesCount = 0;
        totalCurrenciesCount = newCurrencyIds.size();
        
        // Инициализируем и загружаем данные для каждой валюты
        for (Integer currencyId : newCurrencyIds) {
            initializeCurrencyAmount(currencyId);
            loadAccountAmount(currencyId);
        }
    }

    @Override
    protected void recalculateResultAmount() {
        ThreadManager.getExecutor().execute(() -> {
            Integer displayCurrencyId = getDisplayCurrencyId().getValue();
        
            if (displayCurrencyId != null) {
                long totalAmount = 0L;
                
                // Получаем актуальные данные напрямую в фоновом потоке
                for (Map.Entry<Integer, MutableLiveData<Long>> entry : getCurrencyAmounts().entrySet()) {
                    final Integer currencyId = entry.getKey();
                    final Long value = entry.getValue().getValue();
                    
                    if (value != null && value != 0) {
                        long convertedAmount = convertAmountToDisplayCurrency(value, currencyId, displayCurrencyId);
                        totalAmount += convertedAmount;
                        LogManager.d(TAG, "Валюта " + currencyId + ": " + value + " -> " + convertedAmount);
                    }
                }
                
                LogManager.d(TAG, "Пересчет общей суммы счетов (" + accountTypeFilter + "): " + totalAmount);
                // Обновляем UI в главном потоке
                setResultAmount(totalAmount);
            } else {
                LogManager.w(TAG, "displayCurrencyId is null, не можем пересчитать сумму");
                setResultAmount(0L);
            }
        });
    }

    /**
     * Загружает сумму счетов для указанной валюты
     * @param currencyId ID валюты
     */
    private void loadAccountAmount(Integer currencyId) {
        LogManager.d(TAG, "Загрузка суммы счетов для валюты ID: " + currencyId + ", тип: " + accountTypeFilter);
        
        LiveData<Long> serviceAmount = accountService.getTotalAmountByCurrencyAndType(currencyId, accountTypeFilter.getIndex(), EntityFilter.ACTIVE);
        
        if (serviceAmount != null) {
            serviceAmount.observeForever(new Observer<Long>() {
                @Override
                public void onChanged(Long newAmount) {
                    if (newAmount != null) {
                        LogManager.d(TAG, "Валюты ID " + currencyId + " (" + accountTypeFilter + "): сумма " + newAmount);
                        setCurrencyAmount(currencyId, newAmount);
                    } else {
                        LogManager.d(TAG, "Валюты ID " + currencyId + " (" + accountTypeFilter + "): сумма null, устанавливаем 0");
                        setCurrencyAmount(currencyId, 0L);
                    }
                    
                    // Увеличиваем счетчик загруженных валют
                    loadedCurrenciesCount++;
                    LogManager.d(TAG, "Загружено валют: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
                    
                    // Если все валюты загружены, выполняем пересчет
                    if (loadedCurrenciesCount >= totalCurrenciesCount) {
                        LogManager.d(TAG, "Все валюты загружены, выполняем пересчет");
                        ThreadManager.getExecutor().execute(() -> {
                            recalculateResultAmount();
                        });
                    }
                }
            });
        } else {
            LogManager.w(TAG, "accountService.getTotalAmountByCurrencyAndType() вернул null для валюты ID: " + currencyId);
            setCurrencyAmount(currencyId, 0L);
            
            // Увеличиваем счетчик загруженных валют даже для null
            loadedCurrenciesCount++;
            LogManager.d(TAG, "Загружено валют: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
            
            // Если все валюты загружены, выполняем пересчет
            if (loadedCurrenciesCount >= totalCurrenciesCount) {
                LogManager.d(TAG, "Все валюты загружены, выполняем пересчет");
                ThreadManager.getExecutor().execute(() -> {
                    recalculateResultAmount();
                });
            }
        }
    }    
    
    /**
     * Получает фильтр типа счетов
     * @return фильтр типа счетов
     */
    public AccountTypeFilter getAccountTypeFilter() {
        return accountTypeFilter;
    }
    
    /**
     * Получает LiveData с общей суммой всех счетов
     * @return LiveData<Long> общая сумма
     */
    public LiveData<Long> getTotalAmount() {
        return getResultAmount();
    }
}
