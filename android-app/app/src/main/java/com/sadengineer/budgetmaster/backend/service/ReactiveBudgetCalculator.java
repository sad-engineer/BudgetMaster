package com.sadengineer.budgetmaster.backend.service;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Реактивный калькулятор бюджетов с автоматическим пересчетом при изменениях
 * 
 * Автоматически отслеживает изменения в списке валют и суммах бюджетов,
 * пересчитывает общую сумму и предоставляет реактивные данные через LiveData
 */
public class ReactiveBudgetCalculator {

    private static final String TAG = "ReactiveBudgetCalculator";

    private final CurrencyService currencyService;
    private final BudgetService budgetService;
    
    private final MutableLiveData<Long> totalAmount;
    private final MutableLiveData<List<Integer>> currencyIds;
    
    private final Observer<List<Integer>> currencyIdsObserver;
    
    // Храним наблюдатели для каждой валюты отдельно
    private final Map<Integer, Observer<Long>> budgetObservers;

    /**
     * Конструктор
     * 
     * @param currencyService сервис для работы с валютами
     * @param budgetService сервис для работы с бюджетами
     */
    public ReactiveBudgetCalculator(CurrencyService currencyService, BudgetService budgetService) {
        this.currencyService = currencyService;
        this.budgetService = budgetService;
        
        this.totalAmount = new MutableLiveData<>(0L);
        this.currencyIds = new MutableLiveData<>();
        this.budgetObservers = new HashMap<>();
        
        // Инициализируем наблюдатель для списка валют
        this.currencyIdsObserver = new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> newCurrencyIds) {
                Log.d(TAG, "Изменение списка валют: " + newCurrencyIds);
                updateCurrencyObservers(newCurrencyIds);
                recalculateTotalAmount();
            }
        };
        
        // Подписываемся на изменения списка валют
        LiveData<List<Integer>> availableCurrencyIds = currencyService.getAvalibleIds(EntityFilter.ACTIVE);
        if (availableCurrencyIds != null) {
            availableCurrencyIds.observeForever(currencyIdsObserver);
            
            // Инициализируем список валют асинхронно
            ThreadManager.getExecutor().execute(() -> {
                List<Integer> ids = availableCurrencyIds.getValue();
                if (ids != null) {
                    currencyIds.postValue(ids);
                    Log.d(TAG, "Инициализирован список валют: " + ids);
                    
                    // УБИРАЕМ: updateCurrencyObservers(ids) - вызывается только после получения значения
                }
            });
        }
        
        // Добавляем наблюдатель на currencyIds для инициализации
        currencyIds.observeForever(new Observer<List<Integer>>() {
            @Override
            public void onChanged(List<Integer> newCurrencyIds) {
                if (newCurrencyIds != null && !newCurrencyIds.isEmpty()) {
                    Log.d(TAG, "currencyIds получил значение: " + newCurrencyIds);
                    
                    // ВАЖНО: Только после получения значения настраиваем наблюдатели и делаем первый расчет
                    updateCurrencyObservers(newCurrencyIds);
                    
                    // Убираем этот наблюдатель после первого использования
                    currencyIds.removeObserver(this);
                }
            }
        });
    }

    /**
     * Обновить наблюдатели для валют
     */
    private void updateCurrencyObservers(List<Integer> newCurrencyIds) {
        if (newCurrencyIds == null) return;
        
        // Отписываемся от старых наблюдателей
        for (Map.Entry<Integer, Observer<Long>> entry : budgetObservers.entrySet()) {
            if (!newCurrencyIds.contains(entry.getKey())) {
                LiveData<Long> budgetAmount = budgetService.getTotalAmountByCurrency(entry.getKey(), EntityFilter.ACTIVE);
                if (budgetAmount != null) {
                    budgetAmount.removeObserver(entry.getValue());
                }
                budgetObservers.remove(entry.getKey());
            }
        }
        
        // Подписываемся на новые валюты
        for (int currencyId : newCurrencyIds) {
            if (!budgetObservers.containsKey(currencyId)) {
                Observer<Long> observer = new Observer<Long>() {
                    @Override
                    public void onChanged(Long newAmount) {
                        Log.d(TAG, "Изменение бюджета для валюты " + currencyId + ": " + newAmount);
                        recalculateTotalAmount();
                    }
                };
                
                budgetObservers.put(currencyId, observer);
                
                // Подписываемся на изменения бюджета для этой валюты
                LiveData<Long> budgetAmount = budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
                if (budgetAmount != null) {
                    budgetAmount.observeForever(observer);
                    Log.d(TAG, "Подписались на изменения бюджета для валюты " + currencyId);
                }
            }
        }
    }

    /**
     * Пересчитать общую сумму бюджетов
     */
    private void recalculateTotalAmount() {
        List<Integer> currentCurrencyIds = currencyIds.getValue();
        Log.d(TAG, "recalculateTotalAmount вызван, текущие ID валют: " + currentCurrencyIds);
        
        if (currentCurrencyIds == null) {
            Log.w(TAG, "currencyIds.getValue() вернул null - список валют еще не инициализирован");
            return; // Не устанавливаем 0, просто выходим
        }
        
        if (currentCurrencyIds.isEmpty()) {
            Log.w(TAG, "Список валют пуст, устанавливаем 0");
            totalAmount.setValue(0L);
            return;
        }
        
        // Выполняем расчет асинхронно
        ThreadManager.getExecutor().execute(() -> {
            long newTotalAmount = 0L;
            Log.d(TAG, "Начинаем пересчет общей суммы для валют: " + currentCurrencyIds);
            
            for (int currencyId : currentCurrencyIds) {
                LiveData<Long> budgetAmount = budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
                if (budgetAmount != null) {
                    Long amount = budgetAmount.getValue();
                    if (amount != null) {
                        newTotalAmount += amount;
                        Log.d(TAG, "Валюты " + currencyId + ": " + amount + " (сумма: " + newTotalAmount + ")");
                    } else {
                        Log.w(TAG, "Бюджет для валюты " + currencyId + " равен null");
                    }
                } else {
                    Log.w(TAG, "Не удалось получить бюджет для валюты " + currencyId);
                }
            }
            
            Log.d(TAG, "Пересчитана общая сумма бюджетов: " + newTotalAmount);
            totalAmount.postValue(newTotalAmount);
        });
    }

    /**
     * Получить сумму бюджетов по валюте
     * 
     * @param currencyId ID валюты
     * @return LiveData с суммой бюджетов
     */
    public LiveData<Long> getBudgetsAmountByCurrency(int currencyId) {
        return budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
    }

    /**
     * Получить общую сумму всех бюджетов
     * 
     * @return LiveData с общей суммой
     */
    public LiveData<Long> getTotalAmount() {
        return totalAmount;
    }

    /**
     * Получить список ID валют
     * 
     * @return LiveData со списком ID валют
     */
    public LiveData<List<Integer>> getCurrencyIds() {
        return currencyIds;
    }

    /**
     * Обновить список валют и пересчитать суммы
     * 
     * @param newCurrencyIds новый список ID валют
     */
    public void updateCurrencyIds(List<Integer> newCurrencyIds) {
        Log.d(TAG, "Принудительное обновление списка валют: " + newCurrencyIds);
        currencyIds.setValue(newCurrencyIds);
        // Пересчет произойдет автоматически через наблюдатель
    }

    /**
     * Принудительно пересчитать общую сумму
     * Полезно для отладки и принудительного обновления
     */
    public void forceRecalculate() {
        Log.d(TAG, "Принудительный пересчет общей суммы");
        recalculateTotalAmount();
    }

    /**
     * Освободить ресурсы
     * 
     * Важно вызывать при уничтожении объекта для предотвращения утечек памяти
     */
    public void dispose() {
        Log.d(TAG, "Освобождение ресурсов ReactiveBudgetCalculator");
        
        // Отписываемся от списка валют
        LiveData<List<Integer>> availableCurrencyIds = currencyService.getAvalibleIds(EntityFilter.ACTIVE);
        if (availableCurrencyIds != null) {
            availableCurrencyIds.removeObserver(currencyIdsObserver);
        }
        
        // Отписываемся от всех наблюдателей бюджетов
        for (Map.Entry<Integer, Observer<Long>> entry : budgetObservers.entrySet()) {
            LiveData<Long> budgetAmount = budgetService.getTotalAmountByCurrency(entry.getKey(), EntityFilter.ACTIVE);
            if (budgetAmount != null) {
                budgetAmount.removeObserver(entry.getValue());
            }
        }
        
        budgetObservers.clear();
        Log.d(TAG, "Ресурсы освобождены");
    }
}

/*
 * ПРИМЕР ИСПОЛЬЗОВАНИЯ:
 * 
 * // 1. Создание экземпляра
 * ServiceManager serviceManager = ServiceManager.getInstance(context, userName);
 * ReactiveBudgetCalculator calculator = new ReactiveBudgetCalculator(
 *     serviceManager.currencies, 
 *     serviceManager.budgets
 * );
 * 
 * // 2. Подписка на изменения общей суммы
 * calculator.getTotalAmount().observe(this, new Observer<Long>() {
 *     @Override
 *     public void onChanged(Long totalAmount) {
 *         // Обновляем UI с новой общей суммой
 *         updateTotalAmountDisplay(totalAmount);
 *     }
 * });
 * 
 * // 3. Подписка на изменения списка валют
 * calculator.getCurrencyIds().observe(this, new Observer<List<Integer>>() {
 *     @Override
 *     public void onChanged(List<Integer> currencyIds) {
 *         // Обновляем UI с новым списком валют
 *         updateCurrencyIdsDisplay(currencyIds);
 *     }
 * });
 * 
 * // 4. Получение суммы по конкретной валюте
 * int currencyId = 1;
 * calculator.getBudgetsAmountByCurrency(currencyId).observe(this, new Observer<Long>() {
 *     @Override
 *     public void onChanged(Long amount) {
 *         Log.d(TAG, "Сумма бюджета для валюты " + currencyId + " изменилась: " + amount);
 *     }
 * });
 * 
 * // 5. Принудительное обновление списка валют
 * List<Integer> newCurrencyIds = Arrays.asList(1, 2, 3);
 * calculator.updateCurrencyIds(newCurrencyIds);
 * 
 * // 6. Освобождение ресурсов (ВАЖНО!)
 * @Override
 * protected void onDestroy() {
 *     super.onDestroy();
 *     if (calculator != null) {
 *         calculator.dispose();
 *     }
 * }
 * 
 * // АЛЬТЕРНАТИВНО - через FinancialCalculationCoordinator:
 * FinancialCalculationCoordinator coordinator = new FinancialCalculationCoordinator(context, userName);
 * ReactiveBudgetCalculator calculator = coordinator.getBudgetCalculator();
 */
