package com.sadengineer.budgetmaster.calculators;

import android.app.Application;
 

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Базовая ViewModel для выполнения математических операций по валютам для сущностей, которые могут иметь валюты
 * Автоматически отслеживает изменения в базе данных и выполняет математические операции
 */
public abstract class BasicCalculatorForCurrencyItems extends AndroidViewModel {
    
    //имя наследника для логирования
    protected final String TAG = this.getClass().getSimpleName();
    
    /** Имя пользователя по умолчанию */
    //TODO: передлать на получение имени пользователя из SharedPreferences
    private String userName = "default_user";

    /** Результат математической операции для вывода во внешние виджеты */
    private final MutableLiveData<Long> resultAmount = new MutableLiveData<>(0L);
    
    /** Доступные ID валют */
    private final MutableLiveData<List<Integer>> availableCurrencyIds = new MutableLiveData<>();

    /** ID отображаемой валюты (настраивается в настройках) */
    private final MutableLiveData<Integer> displayCurrencyId = new MutableLiveData<>(ModelConstants.DEFAULT_CURRENCY_ID);
    
    /** Карта ID валюты/суммы в валюте для математических операций */
    private final Map<Integer, MutableLiveData<Long>> currencyAmounts = new HashMap<>();
    
    /** Сервисы для работы с данными */
    private final CurrencyService currencyService;
    
    /** Флаг инициализации */
    private boolean isInitialized = false;
    
    /** Observer для отслеживания изменений валют */
    private Observer<List<Integer>> currencyIdsObserver;
    
    /**
     * Конструктор
     * @param application контекст приложения
     */
    public BasicCalculatorForCurrencyItems(@NonNull Application application) {
        super(application);

        // Инициализируем сервисы
        currencyService = new CurrencyService(application, userName);
        
        LogManager.d(TAG, "BasicCalculatorForCurrencyItems создан");
    }

    /**
     * Инициализирует калькулятор и подписывается на изменения данных валюты
     * Должен вызываться после создания ViewModel
     */
    public void initialize() {
        if (isInitialized) {
            LogManager.d(TAG, "Калькулятор уже инициализирован");
            return;
        }
        
        LogManager.d(TAG, "Инициализация калькулятора бюджетов...");
        
        // Подписываемся на изменения списка валют
        LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvailableIds(EntityFilter.ACTIVE);
        
        if (currencyIdsLiveData != null) {
            currencyIdsObserver = new Observer<List<Integer>>() {
                @Override
                public void onChanged(List<Integer> newCurrencyIds) {
                    if (newCurrencyIds != null && !newCurrencyIds.isEmpty()) {
                        LogManager.d(TAG, "Получены ID валют: " + newCurrencyIds);
                        availableCurrencyIds.setValue(newCurrencyIds);
                        updateForNewCurrencyIds(newCurrencyIds);
                    } else {
                        LogManager.d(TAG, "Список валют пуст или null");
                        availableCurrencyIds.setValue(null);
                        clearCurrencyAmounts();
                    }
                }
            };
            currencyIdsLiveData.observeForever(currencyIdsObserver);
        } else {
            LogManager.w(TAG, "currencyService.getAvalibleIds() вернул null");
        }
        
        isInitialized = true;
        LogManager.d(TAG, "Калькулятор инициализирован");
    }

    /**
     * Проверяет, инициализирован ли калькулятор
     * @return true если калькулятор инициализирован
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Обновляет данные при изменении списка валют
     * Реализовать в наследниках, и прописать что конкретно нужно делать при изменении списка валют
     * @param newCurrencyIds новый список ID валют
     */
    protected abstract void updateForNewCurrencyIds(List<Integer> newCurrencyIds);

    /**
     * Пересчитывает результат математической операции
     * Реализовать в наследниках
     * прописать что конкретно нужно делать при необходимости пересчета итоговой суммы
     */
    protected abstract void recalculateResultAmount();
    
    /**
     * Очищает карту ID валюты/сумма
     */
    private void clearCurrencyAmounts() {
        currencyAmounts.clear();
        LogManager.d(TAG, "Суммы по валютам очищены");

    }

    /**
     * Принудительно обновляет данные
     * Перезагружает список валют и суммы бюджетов
     */
    public void refreshData() {
        LogManager.d(TAG, "Принудительное обновление данных...");
        
        if (!isInitialized) {
            initialize();
            return;
        }
        
        // Получаем актуальный список валют
        LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvailableIds(EntityFilter.ACTIVE);
        
        if (currencyIdsLiveData != null) {
            List<Integer> currentIds = currencyIdsLiveData.getValue();
            if (currentIds != null && !currentIds.isEmpty()) {
                LogManager.d(TAG, "Обновляем данные для " + currentIds.size() + " валют");
                updateForNewCurrencyIds(currentIds);
            } else {
                LogManager.d(TAG, "Текущий список валют пуст, ждем обновления...");
            }
        } else {
            LogManager.w(TAG, "Не удалось получить список валют для обновления");
        }
    }

    /**
     * Получает LiveData с результатом математической операции для вывода во внешние виджеты
     * @return LiveData<Long> общая сумма
     */
    public LiveData<Long> getResultAmount() {
        return resultAmount;
    }

    /**
     * Обновляет результат математической операции
     * @param amount новая сумма
     */
    protected void setResultAmount(long amount) {
        resultAmount.postValue(amount);
    }
    
    /**
     * Получает LiveData с суммой для конкретной валюты
     * @param currencyId ID валюты
     * @return LiveData<Long> сумма в валюте
     */
    protected LiveData<Long> getCurrencyAmount(int currencyId) {
        return currencyAmounts.get(currencyId);
    }
    
    /**
     * Устанавливает сумму для конкретной валюты
     * @param currencyId ID валюты
     * @param amount сумма в валюте
     */
    protected void setCurrencyAmount(int currencyId, long amount) {
        MutableLiveData<Long> amountLiveData = currencyAmounts.get(currencyId);
        if (amountLiveData != null) {
            LogManager.d(TAG, "setCurrencyAmount: устанавливаем сумму для валюты " + currencyId + ": " + amount);
            amountLiveData.setValue(amount);
        } else {
            LogManager.w(TAG, "setCurrencyAmount: валюта с ID " + currencyId + " не найдена в карте сумм");
        }
    }
    
    /**
     * Инициализирует LiveData для новой валюты
     * @param currencyId ID валюты
     */
    protected void initializeCurrencyAmount(int currencyId) {
        if (!currencyAmounts.containsKey(currencyId)) {
            currencyAmounts.put(currencyId, new MutableLiveData<>(0L));
            LogManager.d(TAG, "Инициализирована сумма для валюты " + currencyId);
        }
    }
    
    /**
     * Получает все суммы валют для пересчета
     * @return Map<Integer, MutableLiveData<Long>> карта валют и их сумм
     */
    protected Map<Integer, MutableLiveData<Long>> getCurrencyAmounts() {
        return currencyAmounts;
    }

    /**
     * Получает LiveData с ID отображаемой валюты
     * @return LiveData<Integer> ID отображаемой валюты
     */
    public LiveData<Integer> getDisplayCurrencyId() {
        return displayCurrencyId;
    }

     /**
     * Устанавливает ID отображаемой валюты
     * @param currencyId ID валюты для отображения
     */
    public void setDisplayCurrencyId(int currencyId) {
        LogManager.d(TAG, "Установка отображаемой валюты: " + currencyId);
        displayCurrencyId.setValue(currencyId);
        
        // Пересчитываем результат математической операции с новой валютой
        recalculateResultAmount();
    }

    /**
     * Конвертирует сумму из одной валюты в отображаемую валюту
     * @param amount сумма в исходной валюте
     * @param fromCurrencyId ID исходной валюты
     * @param toCurrencyId ID целевой валюты
     * @return конвертированная сумма
     */
    protected long convertAmountToDisplayCurrency(long amount, int fromCurrencyId, int toCurrencyId) {
        if (fromCurrencyId == toCurrencyId) {
            return amount; // Нет необходимости конвертировать
        }
        
        double exchangeRate = getExchangeRate(fromCurrencyId, toCurrencyId);
        BigDecimal result = BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(exchangeRate));  
        return result.longValue();
    }
    
    /**
     * Получает курс обмена между валютами
     * @param fromCurrencyId ID исходной валюты
     * @param toCurrencyId ID целевой валюты
     * @return курс обмена
     */
    private double getExchangeRate(int fromCurrencyId, int toCurrencyId) {
        try {
            // Получаем курс исходной валюты к рублю
            double fromRate = currencyService.getExchangeRateById(fromCurrencyId);
            LogManager.d(TAG, "Получен курс для валюты " + fromCurrencyId + ": " + fromRate);
            
            // Получаем курс целевой валюты к рублю
            double toRate = currencyService.getExchangeRateById(toCurrencyId);
            LogManager.d(TAG, "Получен курс для валюты " + toCurrencyId + ": " + toRate);
            
            // Если курс не найден, используем 1.0
            if (fromRate == 0 || toRate == 0) {
                LogManager.w(TAG, "Курс валюты не найден: from=" + fromCurrencyId + ", to=" + toCurrencyId + ", используем 1.0");
                return 1.0;
            }
            
            // Конвертируем: fromCurrency -> RUB -> toCurrency
            double exchangeRate = fromRate / toRate;
            LogManager.d(TAG, "Курс обмена " + fromCurrencyId + " -> " + toCurrencyId + ": " + exchangeRate + " (fromRate=" + fromRate + ", toRate=" + toRate + ")");
            
            return exchangeRate;
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка получения курса валют: " + e.getMessage(), e);
            return 1.0; // Возвращаем 1.0 в случае ошибки
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        
        // Отписываемся от LiveData для предотвращения утечек памяти
        if (currencyIdsObserver != null) {
            LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvailableIds(EntityFilter.ACTIVE);
            if (currencyIdsLiveData != null) {
                currencyIdsLiveData.removeObserver(currencyIdsObserver);
            }
            currencyIdsObserver = null;
        }
        
        LogManager.d(TAG, "BasicCalculatorForCurrencyItems очищен");
    }
}

/*
 * ИНСТРУКЦИЯ ПО ИСПОЛЬЗОВАНИЮ БАЗОВОГО КАЛЬКУЛЯТОРА:
 * 
 * 1. СОЗДАНИЕ НАСЛЕДНИКА:
 *    public class MyCalculator extends BasicCalculatorForCurrencyItems {
 *        public MyCalculator(@NonNull Application application) {
 *            super(application);
 *        }
 *        
 *        @Override
 *        protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
 *            // Обрабатываем новые валюты
 *            for (Integer currencyId : newCurrencyIds) {
 *                // Загружаем данные для этой валюты
 *                loadDataForCurrency(currencyId);
 *            }
 *        }
 *        
 *        @Override
 *        protected void recalculateResultAmount() {
 *            // Пересчитываем общую сумму
 *            // Реализуйте логику пересчета в зависимости от ваших потребностей
 *            long totalAmount = calculateTotalAmount();
 *            resultAmount.setValue(totalAmount);
 *        }
 *        
 *        private void loadDataForCurrency(int currencyId) {
 *            // Загружаем данные из базы для конкретной валюты
 *            // Реализуйте загрузку данных в зависимости от ваших потребностей
 *        }
 *        
 *        private long calculateTotalAmount() {
 *            // Реализуйте логику расчета общей суммы
 *            return 0L;
 *        }
 *    }
 * 
 * 2. ИСПОЛЬЗОВАНИЕ В Activity/Fragment:
 *    MyCalculator calculator = new ViewModelProvider(this).get(MyCalculator.class);
 *    calculator.initialize();
 *    
 *    // Подписываемся на изменения результата
 *    calculator.getResultAmount().observe(this, totalAmount -> {
 *        // Обновляем UI с общей суммой
 *        updateTotalAmountDisplay(totalAmount);
 *    });
 *    
 *    // Подписываемся на изменения отображаемой валюты
 *    calculator.getDisplayCurrencyId().observe(this, currencyId -> {
 *        LogManager.d(TAG, "Отображаемая валюта: " + currencyId);
 *    });
 *    
 *    // Устанавливаем новую отображаемую валюту
 *    calculator.setDisplayCurrencyId(2);
 * 
 * 3. ДОСТУПНЫЕ МЕТОДЫ ДЛЯ НАСЛЕДНИКОВ:
 *    - getResultAmount() - получить LiveData с общим результатом
 *    - getDisplayCurrencyId() - получить LiveData с отображаемой валютой
 *    - setDisplayCurrencyId(currencyId) - установить отображаемую валюту
 *    - refreshData() - принудительно обновить данные
 *    - initialize() - инициализировать калькулятор
 *    - convertAmountToDisplayCurrency(amount, fromCurrencyId, toCurrencyId) - конвертация валют
 *    - getCurrencyAmount(currencyId) - получить LiveData с суммой валюты
 *    - setCurrencyAmount(currencyId, amount) - установить сумму валюты
 *    - initializeCurrencyAmount(currencyId) - инициализировать LiveData для валюты
 *    - getCurrencyAmounts() - получить все суммы валют
 *    - setResultAmount(amount) - обновить результат
 * 
 * 4. АВТОМАТИЧЕСКАЯ РАБОТА:
 *    - При изменении списка валют автоматически вызывается updateForNewCurrencyIds()
 *    - При изменении отображаемой валюты автоматически вызывается recalculateResultAmount()
 *    - Все суммы автоматически конвертируются в отображаемую валюту
 *    - Все изменения автоматически уведомляют UI через LiveData
 * 
 * 5. ПРИМЕР ПОЛНОГО ИСПОЛЬЗОВАНИЯ:
 * 
 * public class BudgetCalculator extends BasicCalculatorForCurrencyItems {
 *     private final BudgetService budgetService;
 *     
 *     public BudgetCalculator(@NonNull Application application) {
 *         super(application);
 *         budgetService = new BudgetService(application, "default_user");
 *     }
 *     
 *     @Override
 *     protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
 *         for (Integer currencyId : newCurrencyIds) {
 *             initializeCurrencyAmount(currencyId);
 *             // Загружаем бюджеты для этой валюты
 *             loadBudgetsForCurrency(currencyId);
 *         }
 *     }
 *     
 *     @Override
 *     protected void recalculateResultAmount() {
 *         Integer displayCurrencyId = getDisplayCurrencyId().getValue();
 *         
 *         if (displayCurrencyId != null) {
 *             // Собираем данные для конвертации
 *             Map<Integer, Long> amountsToConvert = new HashMap<>();
 *             for (Map.Entry<Integer, MutableLiveData<Long>> entry : getCurrencyAmounts().entrySet()) {
 *                 Long amount = entry.getValue().getValue();
 *                 if (amount != null) {
 *                     amountsToConvert.put(entry.getKey(), amount);
 *                 }
 *             }
 *             
 *             // Выполняем конвертацию в фоновом потоке
 *             ThreadManager.getExecutor().execute(() -> {
 *                 long totalAmount = 0L;
 *                 
 *                 for (Map.Entry<Integer, Long> entry : amountsToConvert.entrySet()) {
 *                     // Конвертируем в отображаемую валюту
 *                     totalAmount += convertAmountToDisplayCurrency(entry.getValue(), entry.getKey(), displayCurrencyId);
 *                 }
 *                 
 *                 // Обновляем UI в главном потоке
 *                 setResultAmount(totalAmount);
 *             });
 *         }
 *     }
 *     
 *     private void loadBudgetsForCurrency(int currencyId) {
 *         // Загружаем бюджеты для валюты и обновляем через setCurrencyAmount()
 *         // Реализуйте загрузку данных в зависимости от ваших потребностей
 *     }
 * }
 */