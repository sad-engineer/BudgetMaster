package com.sadengineer.budgetmaster.calculators;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.filters.EntityFilter;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.entity.KeyValuePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel для расчета общих сумм счетов по валютам и типам
 * Автоматически отслеживает изменения в базе данных и пересчитывает общую сумму
 */
public class AccountCalculatorViewModel extends BasicCalculatorForCurrencyItems {

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
        accountService = new AccountService(application, "default_user");
        this.accountTypeFilter = accountTypeFilter;
        
        Log.d(TAG, "AccountCalculatorViewModel создан с фильтром: " + accountTypeFilter);
    }

    @Override
    protected void updateForNewCurrencyIds(List<Integer> newCurrencyIds) {
        Log.d(TAG, "Обновление сумм счетов для " + newCurrencyIds.size() + " валют, тип: " + accountTypeFilter);
        
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
                        Log.d(TAG, "Валюта " + currencyId + ": " + value + " -> " + convertedAmount);
                    }
                }
                
                Log.d(TAG, "Пересчет общей суммы счетов (" + accountTypeFilter + "): " + totalAmount);
                // Обновляем UI в главном потоке
                setResultAmount(totalAmount);
            } else {
                Log.w(TAG, "displayCurrencyId is null, не можем пересчитать сумму");
                setResultAmount(0L);
            }
        });
    }

    /**
     * Загружает сумму счетов для указанной валюты
     * @param currencyId ID валюты
     */
    private void loadAccountAmount(Integer currencyId) {
        Log.d(TAG, "Загрузка суммы счетов для валюты ID: " + currencyId + ", тип: " + accountTypeFilter);
        
        LiveData<Long> serviceAmount = accountService.getTotalAmountByCurrencyAndType(currencyId, accountTypeFilter.getIndex(), EntityFilter.ACTIVE);
        
        if (serviceAmount != null) {
            serviceAmount.observeForever(new Observer<Long>() {
                @Override
                public void onChanged(Long newAmount) {
                    if (newAmount != null) {
                        Log.d(TAG, "Валюты ID " + currencyId + " (" + accountTypeFilter + "): сумма " + newAmount);
                        setCurrencyAmount(currencyId, newAmount);
                    } else {
                        Log.d(TAG, "Валюты ID " + currencyId + " (" + accountTypeFilter + "): сумма null, устанавливаем 0");
                        setCurrencyAmount(currencyId, 0L);
                    }
                    
                    // Увеличиваем счетчик загруженных валют
                    loadedCurrenciesCount++;
                    Log.d(TAG, "Загружено валют: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
                    
                    // Если все валюты загружены, выполняем пересчет
                    if (loadedCurrenciesCount >= totalCurrenciesCount) {
                        Log.d(TAG, "Все валюты загружены, выполняем пересчет");
                        ThreadManager.getExecutor().execute(() -> {
                            recalculateResultAmount();
                        });
                    }
                }
            });
        } else {
            Log.w(TAG, "accountService.getTotalAmountByCurrencyAndType() вернул null для валюты ID: " + currencyId);
            setCurrencyAmount(currencyId, 0L);
            
            // Увеличиваем счетчик загруженных валют даже для null
            loadedCurrenciesCount++;
            Log.d(TAG, "Загружено валют: " + loadedCurrenciesCount + "/" + totalCurrenciesCount);
            
            // Если все валюты загружены, выполняем пересчет
            if (loadedCurrenciesCount >= totalCurrenciesCount) {
                Log.d(TAG, "Все валюты загружены, выполняем пересчет");
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

/*
 * ИНСТРУКЦИЯ ПО ИСПОЛЬЗОВАНИЮ:
 * 
 * AccountCalculatorViewModel наследуется от BasicCalculatorForCurrencyItems
 * и использует его функциональность для работы с валютами.
 * 
 * 1. СОЗДАНИЕ В StartActivity или Fragment:
 *    // Для текущих счетов
 *    AccountCalculatorViewModel currentAccountsCalculator = new ViewModelProvider(this).get(
 *        AccountCalculatorViewModel.class, 
 *        new ViewModelProvider.Factory() {
 *            @NonNull
 *            @Override
 *            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
 *                return (T) new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.CURRENT);
 *            }
 *        }
 *    );
 *    
 *    // Для сберегательных счетов
 *    AccountCalculatorViewModel savingsAccountsCalculator = new ViewModelProvider(this).get(
 *        AccountCalculatorViewModel.class, 
 *        new ViewModelProvider.Factory() {
 *            @NonNull
 *            @Override
 *            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
 *                return (T) new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.SAVINGS);
 *            }
 *        }
 *    );
 *    
 *    // Для всех счетов
 *    AccountCalculatorViewModel allAccountsCalculator = new ViewModelProvider(this).get(
 *        AccountCalculatorViewModel.class, 
 *        new ViewModelProvider.Factory() {
 *            @NonNull
 *            @Override
 *            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
 *                return (T) new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.ALL);
 *            }
 *        }
 *    );
 * 
 * 2. ИНИЦИАЛИЗАЦИЯ (обязательно после создания):
 *    currentAccountsCalculator.initialize();
 * 
 * 3. ПОДПИСКА НА ИЗМЕНЕНИЯ ОБЩЕЙ СУММЫ:
 *    currentAccountsCalculator.getTotalAmount().observe(this, totalAmount -> {
 *        // totalAmount - общая сумма счетов в отображаемой валюте
 *        // Автоматически обновляется при изменении данных в базе
 *        Log.d(TAG, "Общая сумма текущих счетов: " + totalAmount);
 *    });
 * 
 * 4. УПРАВЛЕНИЕ ОТОБРАЖАЕМОЙ ВАЛЮТОЙ:
 *    // Получить текущую отображаемую валюту
 *    currentAccountsCalculator.getDisplayCurrencyId().observe(this, currencyId -> {
 *        Log.d(TAG, "Отображаемая валюта: " + currencyId);
 *    });
 *    
 *    // Установить новую отображаемую валюту
 *    currentAccountsCalculator.setDisplayCurrencyId(2); // например, доллары
 * 
 * 5. ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ:
 *    currentAccountsCalculator.refreshData();
 * 
 * 6. АВТОМАТИЧЕСКАЯ РАБОТА:
 *    - При изменении списка валют автоматически обновляются суммы счетов
 *    - При изменении любой суммы счета автоматически пересчитывается общая сумма
 *    - При изменении отображаемой валюты автоматически пересчитывается общая сумма
 *    - Все суммы конвертируются в отображаемую валюту через CurrencyConverter в фоновом потоке
 *    - Конвертация валют выполняется через ThreadManager.getExecutor() для безопасности
 *    - Все изменения автоматически уведомляют UI через LiveData
 * 
 * 7. ФИЛЬТРЫ ТИПОВ СЧЕТОВ:
 *    - AccountTypeFilter.CURRENT - только текущие счета
 *    - AccountTypeFilter.SAVINGS - только сберегательные счета
 *    - AccountTypeFilter.CREDIT - только кредитные счета
 *    - AccountTypeFilter.ALL - все типы счетов
 * 
 * ПРИМЕР ПОЛНОГО ИСПОЛЬЗОВАНИЯ:
 * 
 * public class StartActivity extends AppCompatActivity {
 *     private AccountCalculatorViewModel currentAccountsCalculator;
 *     private AccountCalculatorViewModel savingsAccountsCalculator;
 *     
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         
 *         // Создаем калькуляторы для разных типов счетов
 *         currentAccountsCalculator = new ViewModelProvider(this).get(
 *             AccountCalculatorViewModel.class, 
 *             new ViewModelProvider.Factory() {
 *                 @NonNull
 *                 @Override
 *                 public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
 *                     return (T) new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.CURRENT);
 *                 }
 *             }
 *         );
 *         
 *         savingsAccountsCalculator = new ViewModelProvider(this).get(
 *             AccountCalculatorViewModel.class, 
 *             new ViewModelProvider.Factory() {
 *                 @NonNull
 *                 @Override
 *                 public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
 *                     return (T) new AccountCalculatorViewModel(getApplication(), AccountTypeFilter.SAVINGS);
 *                 }
 *             }
 *         );
 *         
 *         // Инициализируем
 *         currentAccountsCalculator.initialize();
 *         savingsAccountsCalculator.initialize();
 *         
 *         // Подписываемся на изменения сумм
 *         currentAccountsCalculator.getTotalAmount().observe(this, totalAmount -> {
 *             updateCurrentAccountsDisplay(totalAmount);
 *         });
 *         
 *         savingsAccountsCalculator.getTotalAmount().observe(this, totalAmount -> {
 *             updateSavingsAccountsDisplay(totalAmount);
 *         });
 *     }
 *     
 *     private void updateCurrentAccountsDisplay(Long totalAmount) {
 *         TextView currentAccountsText = findViewById(R.id.current_accounts_text);
 *         currentAccountsText.setText("Текущие счета: " + totalAmount);
 *     }
 *     
 *     private void updateSavingsAccountsDisplay(Long totalAmount) {
 *         TextView savingsAccountsText = findViewById(R.id.savings_accounts_text);
 *         savingsAccountsText.setText("Сберегательные счета: " + totalAmount);
 *     }
 * }
 */
