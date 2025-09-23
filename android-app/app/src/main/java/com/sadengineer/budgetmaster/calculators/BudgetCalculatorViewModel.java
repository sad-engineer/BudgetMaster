package com.sadengineer.budgetmaster.calculators;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.utils.LogManager;

import java.util.List;
import java.util.Map;

/**
 * ViewModel для расчета общих сумм бюджетов по валютам
 * Автоматически отслеживает изменения в базе данных и пересчитывает общую сумму
 */
public class BudgetCalculatorViewModel extends BasicCalculatorForCurrencyItems {
    
    private static final String TAG = "BudgetCalculatorViewModel";

    /** Сервис для работы с бюджетами */
    private final BudgetService budgetService;
    
    /** Счетчик загруженных валют для отслеживания завершения загрузки */
    private int loadedCurrenciesCount = 0;
    private int totalCurrenciesCount = 0;

    /**
     * Конструктор
     * @param application контекст приложения
     */
    public BudgetCalculatorViewModel(@NonNull Application application) {
        super(application);
        
        // Инициализируем сервис бюджетов
        budgetService = new BudgetService(application, "BudgetCalculator");
        
        LogManager.d(TAG, "BudgetCalculatorViewModel создан");
    }

    @Override
    protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
        LogManager.d(TAG, "Обновление сумм бюджетов для " + newCurrencyIds.size() + " валют");
        
        // Сбрасываем счетчики
        loadedCurrenciesCount = 0;
        totalCurrenciesCount = newCurrencyIds.size();
        
        // Инициализируем и загружаем данные для каждой валюты
        for (Integer currencyId : newCurrencyIds) {
            initializeCurrencyAmount(currencyId);
            loadBudgetAmount(currencyId);
        }
    }

    @Override
    protected void recalculateResultAmount() {
        ThreadManager.getExecutor().execute(() -> {
            // Метод уже вызывается в фоновом потоке, выполняем пересчет напрямую
            Integer displayCurrencyId = getDisplayCurrencyId().getValue();
            
            LogManager.d(TAG, "recalculateResultAmount: displayCurrencyId = " + displayCurrencyId);
            LogManager.d(TAG, "recalculateResultAmount: currencyAmounts.size() = " + getCurrencyAmounts().size());
            
            if (displayCurrencyId != null) {
                long totalAmount = 0L;
                
                // Получаем актуальные данные напрямую в фоновом потоке
                for (Map.Entry<Integer, MutableLiveData<Long>> entry : getCurrencyAmounts().entrySet()) {
                    final Integer currencyId = entry.getKey();
                    final Long value = entry.getValue().getValue();
                    
                    LogManager.d(TAG, "recalculateResultAmount: валюта " + currencyId + " = " + value);
                    
                    if (value != null && value != 0) {
                        long convertedAmount = convertAmountToDisplayCurrency(value, currencyId, displayCurrencyId);
                        totalAmount += convertedAmount;
                        LogManager.d(TAG, "Валюта " + currencyId + ": " + value + " -> " + convertedAmount);
                    }
                }
                
                LogManager.d(TAG, "Пересчет общей суммы бюджетов: " + totalAmount);
                // Обновляем UI в главном потоке
                setResultAmount(totalAmount);
            } else {
                LogManager.w(TAG, "displayCurrencyId is null, не можем пересчитать сумму");
                setResultAmount(0L);
            }
        });
    }

    /**
     * Загружает сумму бюджета для указанной валюты
     * @param currencyId ID валюты
     */
    private void loadBudgetAmount(Integer currencyId) {
        LogManager.d(TAG, "Загрузка суммы бюджета для валюты ID: " + currencyId);
        
        LiveData<Long> serviceAmount = budgetService.getTotalAmountByCurrency(currencyId, EntityFilter.ACTIVE);
        
        if (serviceAmount != null) {
            serviceAmount.observeForever(new Observer<Long>() {
                @Override
                public void onChanged(Long newAmount) {
                    if (newAmount != null) {
                        LogManager.d(TAG, "Валюты ID " + currencyId + ": сумма " + newAmount);
                        setCurrencyAmount(currencyId, newAmount);
                    } else {
                        LogManager.d(TAG, "Валюты ID " + currencyId + ": сумма null, устанавливаем 0");
                        setCurrencyAmount(currencyId, 0L);
                    }
                    
                    // Увеличиваем счетчик загруженных валют
                    loadedCurrenciesCount++;
                    LogManager.d(TAG, "Загружено валют: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
                    
                    // Если все валюты загружены, выполняем пересчет
                    if (loadedCurrenciesCount >= totalCurrenciesCount) {
                        LogManager.d(TAG, "Все валюты загружены, выполняем пересчет");
                        recalculateResultAmount();
                    }
                }
            });
        } else {
            LogManager.w(TAG, "budgetService.getTotalAmountByCurrency() вернул null для валюты ID: " + currencyId);
            setCurrencyAmount(currencyId, 0L);
            
            // Увеличиваем счетчик загруженных валют
            loadedCurrenciesCount++;
            LogManager.d(TAG, "Загружено валют (null): " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
            
            // Если все валюты загружены, выполняем пересчет
            if (loadedCurrenciesCount >= totalCurrenciesCount) {
                LogManager.d(TAG, "Все валюты загружены (null), выполняем пересчет");
                recalculateResultAmount();
            }
        }
    }
}

/*
 * ИНСТРУКЦИЯ ПО ИСПОЛЬЗОВАНИЮ:
 * 
 * BudgetCalculatorViewModel теперь наследуется от BasicCalculatorForCurrencyItems
 * и использует его функциональность для работы с валютами.
 * 
 * 1. СОЗДАНИЕ В StartActivity или Fragment:
 *    BudgetCalculatorViewModel calculatorViewModel = new ViewModelProvider(this).get(BudgetCalculatorViewModel.class);
 * 
 * 2. ИНИЦИАЛИЗАЦИЯ (обязательно после создания):
 *    calculatorViewModel.initialize();
 * 
 * 3. ПОДПИСКА НА ИЗМЕНЕНИЯ ОБЩЕЙ СУММЫ:
 *    calculatorViewModel.getResultAmount().observe(this, totalAmount -> {
 *        // totalAmount - общая сумма всех бюджетов в отображаемой валюте
 *        // Автоматически обновляется при изменении данных в базе
 *        LogManager.d(TAG, "Общая сумма бюджетов: " + totalAmount);
 *    });
 * 
 * 4. УПРАВЛЕНИЕ ОТОБРАЖАЕМОЙ ВАЛЮТОЙ:
 *    // Получить текущую отображаемую валюту
 *    calculatorViewModel.getDisplayCurrencyId().observe(this, currencyId -> {
 *        LogManager.d(TAG, "Отображаемая валюта: " + currencyId);
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
 *    - Все суммы конвертируются в отображаемую валюту через CurrencyConverter в фоновом потоке
 *    - Конвертация валют выполняется через ThreadManager.getExecutor() для безопасности
 *    - Все изменения автоматически уведомляют UI через LiveData
 * 
 * ПРИМЕР ПОЛНОГО ИСПОЛЬЗОВАНИЯ:
 * 
 * public class StartActivity extends AppCompatActivity {
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
 *         calculatorViewModel.getResultAmount().observe(this, totalAmount -> {
 *             // Обновляем UI с новой суммой (уже в отображаемой валюте)
 *             updateTotalAmountDisplay(totalAmount);
 *         });
 *         
 *         // Подписываемся на изменения отображаемой валюты
 *         calculatorViewModel.getDisplayCurrencyId().observe(this, currencyId -> {
 *             LogManager.d(TAG, "Валюта отображения изменена на: " + currencyId);
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
