package com.sadengineer.budgetmaster.calculators;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;
import com.sadengineer.budgetmaster.backend.ThreadManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * ViewModel для расчета общих сумм бюджетов по валютам
 * Автоматически отслеживает изменения в базе данных и пересчитывает общую сумму
 */
public class BudgetCalculatorViewModel extends AndroidViewModel {
    
    private static final String TAG = "BudgetCalculatorViewModel";

    /** Имя пользователя по умолчанию */
    private String userName = "default_user";

    /** Общая сумма всех бюджетов */
    private final MutableLiveData<Long> totalAmount = new MutableLiveData<>(0L);
    
    /** Суммы бюджетов для каждой валюты */
    private final Map<Integer, MutableLiveData<Long>> currencyAmounts = new HashMap<>();
    
    /** Доступные ID валют */
    private final MutableLiveData<List<Integer>> availableCurrencyIds = new MutableLiveData<>();
    
    /** ID отображаемой валюты (настраивается в настройках) */
    private final MutableLiveData<Integer> displayCurrencyId = new MutableLiveData<>(ModelConstants.DEFAULT_CURRENCY_ID);
    
    /** Сервисы для работы с данными */
    private final CurrencyService currencyService;
    private final BudgetService budgetService;
    
    /** Флаг инициализации */
    private boolean isInitialized = false;

    /**
     * Конструктор
     * @param application контекст приложения
     */
    public BudgetCalculatorViewModel(@NonNull Application application) {
        super(application);
        
        // Инициализируем сервисы
        currencyService = new CurrencyService(application, userName);
        budgetService = new BudgetService(application, userName);
        
        Log.d(TAG, "BudgetCalculatorViewModel создан");
    }

    /**
     * Инициализирует калькулятор и подписывается на изменения данных
     * Должен вызываться после создания ViewModel
     */
    public void initialize() {
        if (isInitialized) {
            Log.d(TAG, "Калькулятор уже инициализирован");
            return;
        }
        
        Log.d(TAG, "Инициализация калькулятора бюджетов...");
        
        // Подписываемся на изменения списка валют
        LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvalibleIds(EntityFilter.ACTIVE);
        
        if (currencyIdsLiveData != null) {
            currencyIdsLiveData.observeForever(new Observer<List<Integer>>() {
                @Override
                public void onChanged(List<Integer> newCurrencyIds) {
                    if (newCurrencyIds != null && !newCurrencyIds.isEmpty()) {
                        Log.d(TAG, "Получены ID валют: " + newCurrencyIds);
                        availableCurrencyIds.setValue(newCurrencyIds);
                        updateBudgetAmounts(newCurrencyIds);
                    } else {
                        Log.d(TAG, "Список валют пуст или null");
                        availableCurrencyIds.setValue(null);
                        clearCurrencyAmounts();
                    }
                }
            });
        } else {
            Log.w(TAG, "currencyService.getAvalibleIds() вернул null");
        }
        
        isInitialized = true;
        Log.d(TAG, "Калькулятор инициализирован");
    }

    /**
     * Обновляет суммы бюджетов для указанных валют
     * @param currencyIds список ID валют
     */
    private void updateBudgetAmounts(List<Integer> currencyIds) {
        Log.d(TAG, "Обновление сумм бюджетов для " + currencyIds.size() + " валют");
        
        // Очищаем старые наблюдения
        clearCurrencyAmounts();
        
        // Создаем новые MutableLiveData для каждой валюты
        for (Integer currencyId : currencyIds) {
            MutableLiveData<Long> amountLiveData = new MutableLiveData<>(0L);
            currencyAmounts.put(currencyId, amountLiveData);
            
                    // Подписываемся на изменения суммы для этой валюты
        amountLiveData.observeForever(new Observer<Long>() {
            @Override
            public void onChanged(Long newAmount) {
                // Откладываем вызов в фоновый поток
                ThreadManager.getExecutor().execute(() -> {
                    recalculateTotalAmount();
                });
            }
        });
            
            // Загружаем данные из сервиса
            loadBudgetAmount(currencyId, amountLiveData);
        }
    }

    /**
     * Загружает сумму бюджета для указанной валюты
     * @param currencyId ID валюты
     * @param amountLiveData LiveData для хранения суммы
     */
    private void loadBudgetAmount(Integer currencyId, MutableLiveData<Long> amountLiveData) {
        Log.d(TAG, "Загрузка суммы бюджета для валюты ID: " + currencyId);
        
        LiveData<Long> serviceAmount = budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
        
        if (serviceAmount != null) {
            serviceAmount.observeForever(new Observer<Long>() {
                @Override
                public void onChanged(Long newAmount) {
                    if (newAmount != null) {
                        Log.d(TAG, "Валюты ID " + currencyId + ": сумма " + newAmount);
                        amountLiveData.setValue(newAmount);
                    } else {
                        Log.d(TAG, "Валюты ID " + currencyId + ": сумма null, устанавливаем 0");
                        amountLiveData.setValue(0L);
                    }
                }
            });
        } else {
            Log.w(TAG, "budgetService.getTotalAmountByCurrency() вернул null для валюты ID: " + currencyId);
            amountLiveData.setValue(0L);
        }
    }

    /**
     * Пересчитывает общую сумму всех бюджетов с учетом курсов валют
     */
    private void recalculateTotalAmount() {
        final Integer displayCurrency = displayCurrencyId.getValue() != null ? 
            displayCurrencyId.getValue() : ModelConstants.DEFAULT_CURRENCY_ID;
        
        // Убираем лишний ThreadManager.getExecutor().execute() - метод уже вызывается в фоновом потоке
        long total = 0;
        
        for (Map.Entry<Integer, MutableLiveData<Long>> entry : currencyAmounts.entrySet()) {
            final Integer currencyId = entry.getKey();
            final Long value = entry.getValue().getValue();
            
            if (value != null && value != 0) {
                long convertedAmount = convertAmountToDisplayCurrency(value, currencyId, displayCurrency);
                total += convertedAmount;
                Log.d(TAG, "Валюта " + currencyId + ": " + value + " -> " + convertedAmount + " (курс: " + getExchangeRate(currencyId, displayCurrency) + ")");
            }
        }
        
        Log.d(TAG, "Пересчет общей суммы в валюте " + displayCurrency + ": " + total);
        
        // Обновляем UI в главном потоке
        totalAmount.postValue(total);
    }
    
    //TODO логика перевода валют должна быть не тут
    /**
     * Конвертирует сумму из одной валюты в отображаемую валюту
     * @param amount сумма в исходной валюте
     * @param fromCurrencyId ID исходной валюты
     * @param toCurrencyId ID целевой валюты
     * @return конвертированная сумма
     */
    private long convertAmountToDisplayCurrency(long amount, int fromCurrencyId, int toCurrencyId) {
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
            Log.d(TAG, "Получен курс для валюты " + fromCurrencyId + ": " + fromRate);
            
            // Получаем курс целевой валюты к рублю
            double toRate = currencyService.getExchangeRateById(toCurrencyId);
            Log.d(TAG, "Получен курс для валюты " + toCurrencyId + ": " + toRate);
            
            // Если курс не найден, используем 1.0
            if (fromRate == 0 || toRate == 0) {
                Log.w(TAG, "Курс валюты не найден: from=" + fromCurrencyId + ", to=" + toCurrencyId + ", используем 1.0");
                return 1.0;
            }
            
            // Конвертируем: fromCurrency -> RUB -> toCurrency
            double exchangeRate = fromRate / toRate;
            Log.d(TAG, "Курс обмена " + fromCurrencyId + " -> " + toCurrencyId + ": " + exchangeRate + " (fromRate=" + fromRate + ", toRate=" + toRate + ")");
            
            return exchangeRate;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения курса валют: " + e.getMessage(), e);
            return 1.0; // Возвращаем 1.0 в случае ошибки
        }
    }

    /**
     * Очищает все суммы по валютам
     */
    private void clearCurrencyAmounts() {
        currencyAmounts.clear();
        Log.d(TAG, "Суммы по валютам очищены");
    }

    /**
     * Принудительно обновляет данные
     * Перезагружает список валют и суммы бюджетов
     */
    public void refreshData() {
        Log.d(TAG, "Принудительное обновление данных...");
        
        if (!isInitialized) {
            Log.w(TAG, "Калькулятор не инициализирован, вызываем initialize()");
            initialize();
            return;
        }
        
        // Получаем актуальный список валют
        LiveData<List<Integer>> currencyIdsLiveData = currencyService.getAvalibleIds(EntityFilter.ACTIVE);
        
        if (currencyIdsLiveData != null) {
            List<Integer> currentIds = currencyIdsLiveData.getValue();
            if (currentIds != null && !currentIds.isEmpty()) {
                Log.d(TAG, "Обновляем данные для " + currentIds.size() + " валют");
                updateBudgetAmounts(currentIds);
            } else {
                Log.d(TAG, "Текущий список валют пуст, ждем обновления...");
            }
        } else {
            Log.w(TAG, "Не удалось получить список валют для обновления");
        }
    }

    /**
     * Получает LiveData с общей суммой всех бюджетов
     * @return LiveData<Long> общая сумма
     */
    public LiveData<Long> getTotalAmount() {
        return totalAmount;
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
        Log.d(TAG, "Установка отображаемой валюты: " + currencyId);
        displayCurrencyId.setValue(currencyId);
        
        // Пересчитываем общую сумму с новой валютой
        recalculateTotalAmount();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "BudgetCalculatorViewModel очищен");
    }
}

/*
 * ИНСТРУКЦИЯ ПО ИСПОЛЬЗОВАНИЮ:
 * 
 * 1. СОЗДАНИЕ В MainActivity или Fragment:
 *    BudgetCalculatorViewModel calculatorViewModel = new ViewModelProvider(this).get(BudgetCalculatorViewModel.class);
 * 
 * 2. ИНИЦИАЛИЗАЦИЯ (обязательно после создания):
 *    calculatorViewModel.initialize();
 * 
 * 3. ПОДПИСКА НА ИЗМЕНЕНИЯ ОБЩЕЙ СУММЫ:
 *    calculatorViewModel.getTotalAmount().observe(this, totalAmount -> {
 *        // totalAmount - общая сумма всех бюджетов в отображаемой валюте
 *        // Автоматически обновляется при изменении данных в базе
 *        Log.d(TAG, "Общая сумма бюджетов: " + totalAmount);
 *    });
 * 
 * 4. УПРАВЛЕНИЕ ОТОБРАЖАЕМОЙ ВАЛЮТОЙ:
 *    // Получить текущую отображаемую валюту
 *    calculatorViewModel.getDisplayCurrencyId().observe(this, currencyId -> {
 *        Log.d(TAG, "Отображаемая валюта: " + currencyId);
 *    });
 *    
 *    // Установить новую отображаемую валюту
 *    calculatorViewModel.setDisplayCurrencyId(2); // например, доллары
 * 
 * 5. ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ:
 *    calculatorViewModel.refreshData();
 * 
 * 6. АВТОМАТИЧЕСКАЯ РАБОТА:
 *    - При изменении списка валют автоматически обновляются суммы бюджетов
 *    - При изменении любой суммы бюджета автоматически пересчитывается общая сумма
 *    - При изменении отображаемой валюты автоматически пересчитывается общая сумма
 *    - Все суммы конвертируются в отображаемую валюту через курсы обмена
 *    - Все изменения автоматически уведомляют UI через LiveData
 * 
 * ПРИМЕР ПОЛНОГО ИСПОЛЬЗОВАНИЯ:
 * 
 * public class MainActivity extends AppCompatActivity {
 *     private BudgetCalculatorViewModel calculatorViewModel;
 *     
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         
 *         // Создаем ViewModel
 *         calculatorViewModel = new ViewModelProvider(this).get(BudgetCalculatorViewModel.class);
 *         
 *         // Инициализируем
 *         calculatorViewModel.initialize();
 *         
 *         // Подписываемся на изменения общей суммы
 *         calculatorViewModel.getTotalAmount().observe(this, totalAmount -> {
 *             // Обновляем UI с новой суммой (уже в отображаемой валюте)
 *             updateTotalAmountDisplay(totalAmount);
 *         });
 *         
 *         // Подписываемся на изменения отображаемой валюты
 *         calculatorViewModel.getDisplayCurrencyId().observe(this, currencyId -> {
 *             Log.d(TAG, "Валюта отображения изменена на: " + currencyId);
 *         });
 *     }
 *     
 *     private void updateTotalAmountDisplay(Long totalAmount) {
 *         // Обновляем отображение общей суммы
 *         TextView totalAmountText = findViewById(R.id.total_amount_text);
 *         totalAmountText.setText("Общая сумма: " + totalAmount);
 *     }
 *     
 *     // Метод для смены отображаемой валюты (например, из настроек)
 *     private void changeDisplayCurrency(int newCurrencyId) {
 *         calculatorViewModel.setDisplayCurrencyId(newCurrencyId);
 *     }
 * }
 */
