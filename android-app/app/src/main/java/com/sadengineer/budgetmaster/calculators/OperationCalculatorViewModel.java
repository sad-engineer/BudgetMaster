package com.sadengineer.budgetmaster.calculators;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.OperationTypeFilter;
import com.sadengineer.budgetmaster.backend.service.OperationService;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.filters.OperationPeriod;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

/**
 * ViewModel для калькулятора операций
 * Наследуется от BasicCalculatorForCurrencyItems для поддержки конвертации валют
 */
public class OperationCalculatorViewModel extends BasicCalculatorForCurrencyItems {
    
    private static final String TAG = "OperationCalculatorViewModel";
    
    private final OperationService operationService;
    
    // Конфигурация калькулятора
    private OperationCalculatorConfig config;
    
    // Счетчики для отслеживания загрузки данных
    private int loadedCurrenciesCount = 0;
    private int totalCurrenciesCount = 0;
    
    public OperationCalculatorViewModel(Application application) {
        super(application);
        this.operationService = new OperationService(application, "OperationCalculator");
        this.config = new OperationCalculatorConfig(); // Конфигурация по умолчанию
    }
    
    /**
     * Установить конфигурацию калькулятора
     * @param config новая конфигурация
     */
    public void setConfig(OperationCalculatorConfig config) {
        if (config == null || !config.isValid()) {
            Log.e(TAG, "Invalid config provided");
            return;
        }
        
        this.config = config;
        Log.d(TAG, "Config updated: " + config.toString());
        
        // Перезагружаем данные с новой конфигурацией
        loadOperationAmounts();
    }
    
    /**
     * Получить текущую конфигурацию
     * @return текущая конфигурация
     */
    public OperationCalculatorConfig getConfig() {
        return config;
    }
    
    /**
     * Установить период расчета
     * @param period период расчета
     */
    public void setPeriod(OperationPeriod period) {
        if (period != null) {
            config.setPeriod(period);
            loadOperationAmounts();
        }
    }
    
    /**
     * Установить базовую дату
     * @param baseDate базовая дата
     */
    public void setBaseDate(LocalDate baseDate) {
        if (baseDate != null) {
            config.setBaseDate(baseDate);
            loadOperationAmounts();
        }
    }
    
    /**
     * Установить тип операций
     * @param operationType тип операций
     */
    public void setOperationType(OperationTypeFilter operationType) {
        if (operationType != null) {
            config.setOperationType(operationType);
            loadOperationAmounts();
        }
    }
    
    /**
     * Установить категорию
     * @param categoryId ID категории (null = все категории)
     */
    public void setCategoryId(Integer categoryId) {
        config.setCategoryId(categoryId);
        loadOperationAmounts();
    }
    
    /**
     * Установить валюту
     * @param currencyId ID валюты
     */
    public void setCurrencyId(int currencyId) {
        config.setCurrencyId(currencyId);
        loadOperationAmounts();
    }
    
    /**
     * Установить фильтр сущностей
     * @param entityFilter фильтр сущностей
     */
    public void setEntityFilter(EntityFilter entityFilter) {
        if (entityFilter != null) {
            config.setEntityFilter(entityFilter);
            loadOperationAmounts();
        }
    }
    
    /**
     * Загрузить суммы операций по валютам
     */
    private void loadOperationAmounts() {
        if (!isInitialized()) {
            Log.d(TAG, "Calculator not initialized, skipping loadOperationAmounts");
            return;
        }

        Log.d(TAG, "Loading operation amounts for config: " + config.toString());

        try {
            // Если указана конкретная валюта (не 0), загружаем только её
            if (config.getCurrencyId() != 0) {
                loadOperationAmountForCurrency(config.getCurrencyId());
                return;
            }
            
            // Получаем все валюты из уже инициализированных
            Map<Integer, MutableLiveData<Long>> currencyAmounts = getCurrencyAmounts();
            totalCurrenciesCount = currencyAmounts.size();
            loadedCurrenciesCount = 0;
            
            Log.d(TAG, "Total currencies to load: " + totalCurrenciesCount);
            
            // Загружаем суммы для каждой валюты
            for (Map.Entry<Integer, MutableLiveData<Long>> entry : currencyAmounts.entrySet()) {
                Integer currencyId = entry.getKey();
                loadOperationAmountForCurrency(currencyId);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading operation amounts", e);
        }
    }
    
    /**
     * Загрузить сумму операций для конкретной валюты
     * @param currencyId ID валюты
     */
    private void loadOperationAmountForCurrency(int currencyId) {
        try {
            // Создаем конфигурацию для конкретной валюты
            OperationCalculatorConfig currencyConfig = new OperationCalculatorConfig(
                config.getPeriod(),
                config.getBaseDate(),
                config.getOperationType(),
                config.getCategoryId(),
                currencyId, // Указываем конкретную валюту
                config.getEntityFilter()
            );
            
            // Получаем сумму операций для этой валюты
            LiveData<Long> amountLiveData = operationService.getTotalAmountByConfig(currencyConfig);
            
            // Подписываемся на результат
            amountLiveData.observeForever(amount -> {
                if (amount != null) {
                    Log.d(TAG, "Loaded amount for currency " + currencyId + ": " + amount);
                    setCurrencyAmount(currencyId, amount);
                    
                    loadedCurrenciesCount++;
                    Log.d(TAG, "Loaded currencies: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
                    
                    // Если загрузили все валюты, пересчитываем результат
                    if (loadedCurrenciesCount >= totalCurrenciesCount) {
                        recalculateResultAmount();
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading operation amount for currency " + currencyId, e);
            loadedCurrenciesCount++;
        }
    }
    
    @Override
    protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
        Log.d(TAG, "Обновление сумм операций для " + newCurrencyIds.size() + " валют");
        
        // Сбрасываем счетчики
        loadedCurrenciesCount = 0;
        totalCurrenciesCount = newCurrencyIds.size();
        
        // Инициализируем и загружаем данные для каждой валюты
        for (Integer currencyId : newCurrencyIds) {
            initializeCurrencyAmount(currencyId);
            loadOperationAmountForCurrency(currencyId);
        }
    }
    
    /**
     * Пересчитать итоговую сумму
     */
    @Override
    protected void recalculateResultAmount() {
        ThreadManager.getExecutor().execute(() -> {
            Log.d(TAG, "Recalculating result amount");
            
            try {
                // Получаем все суммы по валютам
                Map<Integer, MutableLiveData<Long>> currencyAmounts = getCurrencyAmounts();
                
                if (currencyAmounts.isEmpty()) {
                    Log.d(TAG, "No currency amounts available for recalculation");
                    setResultAmount(0L);
                    return;
                }
                
                // Конвертируем все суммы в валюту вывода
                long totalAmount = 0L;
                Integer outputCurrencyId = getDisplayCurrencyId().getValue();
                
                for (Map.Entry<Integer, MutableLiveData<Long>> entry : currencyAmounts.entrySet()) {
                    Integer currencyId = entry.getKey();
                    Long amount = entry.getValue().getValue();
                    
                    if (amount != null && amount > 0) {
                        if (currencyId.equals(outputCurrencyId)) {
                            // Если валюта совпадает с валютой вывода, просто добавляем
                            totalAmount += amount;
                        } else {
                            // Конвертируем в валюту вывода
                            long convertedAmount = convertAmountToDisplayCurrency(amount, currencyId, outputCurrencyId);
                            totalAmount += convertedAmount;
                        }
                    }
                }
                
                Log.d(TAG, "Recalculated total amount: " + totalAmount);
                setResultAmount(totalAmount);
                
            } catch (Exception e) {
                Log.e(TAG, "Error recalculating result amount", e);
                setResultAmount(0L);
            }
        });
    }
    
    /**
     * Создать конфигурацию для расчета за день
     * @param date дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для дня
     */
    public static OperationCalculatorConfig createDayConfig(LocalDate date, 
                                                          OperationTypeFilter operationType, 
                                                          int currencyId,
                                                          EntityFilter entityFilter) {
        return OperationCalculatorConfig.forDay(date, operationType, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета за месяц
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для месяца
     */
    public static OperationCalculatorConfig createMonthConfig(LocalDate baseDate, 
                                                            OperationTypeFilter operationType, 
                                                            int currencyId,
                                                            EntityFilter entityFilter) {
        return OperationCalculatorConfig.forMonth(baseDate, operationType, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета по категории за месяц
     * @param baseDate базовая дата
     * @param categoryId ID категории
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для категории за месяц
     */
    public static OperationCalculatorConfig createCategoryByMonthConfig(LocalDate baseDate, 
                                                                      Integer categoryId, 
                                                                      int currencyId,
                                                                      EntityFilter entityFilter) {
        return OperationCalculatorConfig.forCategoryByMonth(baseDate, categoryId, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета за 6 месяцев
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для 6 месяцев
     */
    public static OperationCalculatorConfig createSixMonthsConfig(LocalDate baseDate, 
                                                                OperationTypeFilter operationType, 
                                                                int currencyId,
                                                                EntityFilter entityFilter) {
        return OperationCalculatorConfig.forSixMonths(baseDate, operationType, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета за 9 месяцев
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для 9 месяцев
     */
    public static OperationCalculatorConfig createNineMonthsConfig(LocalDate baseDate, 
                                                                 OperationTypeFilter operationType, 
                                                                 int currencyId,
                                                                 EntityFilter entityFilter) {
        return OperationCalculatorConfig.forNineMonths(baseDate, operationType, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета за год
     * @param baseDate базовая дата
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для года
     */
    public static OperationCalculatorConfig createYearConfig(LocalDate baseDate, 
                                                           OperationTypeFilter operationType, 
                                                           int currencyId,
                                                           EntityFilter entityFilter) {
        return OperationCalculatorConfig.forYear(baseDate, operationType, currencyId, entityFilter);
    }
    
    /**
     * Создать конфигурацию для расчета за все время
     * @param operationType тип операций
     * @param currencyId ID валюты
     * @param entityFilter фильтр сущностей
     * @return конфигурация для всего времени
     */
    public static OperationCalculatorConfig createAllTimeConfig(OperationTypeFilter operationType, 
                                                              int currencyId,
                                                              EntityFilter entityFilter) {
        return OperationCalculatorConfig.forAllTime(operationType, currencyId, entityFilter);
    }
    
    
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "OperationCalculatorViewModel cleared");
    }
}
