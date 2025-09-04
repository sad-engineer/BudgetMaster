package com.sadengineer.budgetmaster.start;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.calculators.BudgetCalculatorViewModel;
import com.sadengineer.budgetmaster.calculators.AccountCalculatorViewModel;
import com.sadengineer.budgetmaster.settings.AppSettings;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;

/**
 * ViewModel для стартового экрана
 * Управляет данными и бизнес-логикой стартового экрана
 */
public class StartScreenViewModel extends AndroidViewModel {
    private static final String TAG = "StartScreenViewModel";
    
    private final MainScreenRepository repository;
    private final MutableLiveData<Boolean> isRefreshing;
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    private BudgetCalculatorViewModel budgetCalculator;
    private AccountCalculatorViewModel currentAccountsCalculator;
    private AccountCalculatorViewModel savingsAccountsCalculator;
    private AppSettings appSettings;

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    public StartScreenViewModel(@NonNull Application application) {
        super(application);
        
        this.repository = new MainScreenRepository(application, userName);
        this.isRefreshing = new MutableLiveData<>(false);
        
        // Инициализируем настройки приложения
        this.appSettings = new AppSettings(application);
        
        // Инициализируем калькулятор бюджетов
        this.budgetCalculator = new BudgetCalculatorViewModel(application);
        this.budgetCalculator.initialize();
        
        // Инициализируем калькуляторы счетов
        this.currentAccountsCalculator = new AccountCalculatorViewModel(application, AccountTypeFilter.CURRENT);
        this.currentAccountsCalculator.initialize();
        
        this.savingsAccountsCalculator = new AccountCalculatorViewModel(application, AccountTypeFilter.SAVINGS);
        this.savingsAccountsCalculator.initialize();
        
        Log.d(TAG, "StartScreenViewModel инициализирован");
    }
    
    /**
     * Получить данные стартового экрана
     */
    public LiveData<MainScreenData> getMainScreenData() {
        return repository.getMainScreenData();
    }
    
    /**
     * Получить состояние загрузки
     */
    public LiveData<Boolean> getIsLoading() {
        return repository.getIsLoading();
    }
    
    /**
     * Получить сообщение об ошибке
     */
    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }
    
    /**
     * Получить состояние обновления (для pull-to-refresh)
     */
    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }
    
    /**
     * Обновить все данные главного экрана
     */
    public void refreshData() {
        Log.d(TAG, "Запрос на обновление данных главного экрана");
        isRefreshing.setValue(true);
        
        repository.refreshData();
        
        // Сбрасываем состояние обновления после небольшой задержки
        // чтобы показать анимацию pull-to-refresh
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isRefreshing.setValue(false);
        }, 1000);
    }
    
    /**
     * Обновить конкретное поле данных
     */
    public void updateField(String fieldName, long value) {
        repository.updateField(fieldName, value);
    }
    
    /**
     * Обновить данные при изменении в других экранах
     * Вызывается при возврате на главный экран
     */
    public void onResume() {
        Log.d(TAG, "Главный экран возобновлен, обновляем данные");
        refreshData();
    }
    
    /**
     * Получить отформатированную сумму для отображения
     */
    public String getFormattedTotalAccountsBalance() {
        if (currentAccountsCalculator != null) {
            Long totalAmount = currentAccountsCalculator.getResultAmount().getValue();
            if (totalAmount != null) {
                return formatter.formatFromCents(totalAmount);
            }
        }
        return "0.00";
    }
    
    public String getFormattedMonthlyEarned() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getMonthlyEarned());
    }
    
    public String getFormattedTotalSavingsBalance() {
        if (savingsAccountsCalculator != null) {
            Long totalAmount = savingsAccountsCalculator.getResultAmount().getValue();
            if (totalAmount != null) {
                return formatter.formatFromCents(totalAmount);
            }
        }
        return "0.00";
    }
    
    public String getFormattedTotalBudgetRemaining() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getTotalBudgetRemaining());
    }
    
    public String getFormattedReserveAmount() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getReserveAmount());
    }
    
    /**
     * Получить общую сумму всех бюджетов из калькулятора
     */
    public LiveData<Long> getTotalBudgetAmount() {
        return budgetCalculator.getResultAmount();
    }
    
    /**
     * Получить общую сумму текущих счетов из калькулятора
     */
    public LiveData<Long> getCurrentTotalAccountsAmount() {
        if (currentAccountsCalculator != null) {
            return currentAccountsCalculator.getResultAmount();
        }
        return new MutableLiveData<>(0L);
    }
    
    /**
     * Получить общую сумму сберегательных счетов из калькулятора
     */
    public LiveData<Long> getSavingsTotalAccountsAmount() {
        if (savingsAccountsCalculator != null) {
            return savingsAccountsCalculator.getResultAmount();
        }
        return new MutableLiveData<>(0L);
    }
    
    /**
     * Получить отформатированную общую сумму бюджетов для отладки
     */
    public String getFormattedTotalBudgetAmount() {
        Long totalAmount = getTotalBudgetAmount().getValue();
        if (totalAmount == null) return "0.00";
        return formatter.formatFromCents(totalAmount);
    }
    
    /**
     * Получить LiveData с отформатированной общей суммой бюджетов
     * Автоматически обновляется при изменении totalAmount в BudgetCalculatorViewModel
     */
    public LiveData<String> getFormattedTotalBudgetAmountLiveData() {
        MutableLiveData<String> formattedAmount = new MutableLiveData<>("0.00");
        
        // Подписываемся на изменения общей суммы и форматируем её
        budgetCalculator.getResultAmount().observeForever(totalAmount -> {
            if (totalAmount != null) {
                String formatted = formatter.formatFromCents(totalAmount);
                formattedAmount.setValue(formatted);
                Log.d(TAG, "Общая сумма бюджетов обновлена: " + formatted);
            } else {
                formattedAmount.setValue("0.00");
            }
        });
        
        return formattedAmount;
    }    
    
    /**
     * Получить цвет для отображения суммы (положительная/отрицательная)
     */
    public int getAmountColor(long amount) {
        if (amount >= 0) {
            return ContextCompat.getColor(getApplication(), R.color.green);
        } else {
            return ContextCompat.getColor(getApplication(), R.color.red);
        }
    }
    
    /**
     * Получить цвет для остатка бюджета
     */
    public int getBudgetRemainingColor() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return ContextCompat.getColor(getApplication(), R.color.gray);
        
        long remaining = data.getTotalBudgetRemaining();
        if (remaining > 0) {
            return ContextCompat.getColor(getApplication(), R.color.green);
        } else if (remaining == 0) {
            return ContextCompat.getColor(getApplication(), R.color.orange);
        } else {
            return ContextCompat.getColor(getApplication(), R.color.red);
        }
    }
    
    /**
     * Проверить, есть ли ошибки
     */
    public boolean hasErrors() {
        String error = getErrorMessage().getValue();
        return error != null && !error.isEmpty();
    }
    
    /**
     * Очистить ошибки
     */
    public void clearErrors() {
        // Ошибки очищаются автоматически при следующем обновлении
    }
    
    /**
     * Принудительно обновить данные калькулятора бюджетов
     * Полезно для отладки и тестирования
     */
    public void forceRecalculateBudgets() {
        if (budgetCalculator != null) {
            budgetCalculator.refreshData();
            Log.d(TAG, "Запрошено принудительное обновление данных калькулятора");
        }
    }

    /**Принудительно обновить данные калькулятора текущих счетов
     * Полезно для отладки и тестирования
     */
    public void forceRecalculateCurrentAccounts() {
        if (currentAccountsCalculator != null) {
            currentAccountsCalculator.refreshData();
            Log.d(TAG, "Запрошено принудительное обновление данных калькулятора текущих счетов");
        }
    }

    /**Принудительно обновить данные калькулятора сберегательных счетов
     * Полезно для отладки и тестирования
     */
    public void forceRecalculateSavingsAccounts() {
        if (savingsAccountsCalculator != null) {
            savingsAccountsCalculator.refreshData();
            Log.d(TAG, "Запрошено принудительное обновление данных калькулятора сберегательных счетов");
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        
        // Освобождаем ресурсы калькуляторов
        if (budgetCalculator != null) {
            // ViewModel автоматически очищается системой
            Log.d(TAG, "Калькулятор бюджетов будет очищен системой");
        }
        
        if (currentAccountsCalculator != null) {
            Log.d(TAG, "Калькулятор текущих счетов будет очищен системой");
        }
        
        if (savingsAccountsCalculator != null) {
            Log.d(TAG, "Калькулятор сберегательных счетов будет очищен системой");
        }
        
        Log.d(TAG, "StartScreenViewModel очищен");
    }
    
    /**
     * Устанавливает отображаемую валюту для калькулятора бюджетов
     * @param currencyId ID валюты для отображения
     */
    public void setBudgetCalculatorDisplayCurrency(int currencyId) {
        if (budgetCalculator != null) {
            budgetCalculator.setDisplayCurrencyId(currencyId);
            Log.d(TAG, "Установлена отображаемая валюта для калькулятора: " + currencyId);
        }
    }
    
    /**
     * Получить калькулятор бюджетов
     * @return BudgetCalculatorViewModel для бюджетов
     */
    public BudgetCalculatorViewModel getBudgetCalculator() {
        return budgetCalculator;
    }
    
    /**
     * Получить калькулятор текущих счетов
     * @return AccountCalculatorViewModel для текущих счетов
     */
    public AccountCalculatorViewModel getCurrentAccountsCalculator() {
        return currentAccountsCalculator;
    }
    
    /**
     * Получить калькулятор сберегательных счетов
     * @return AccountCalculatorViewModel для сберегательных счетов
     */
    public AccountCalculatorViewModel getSavingsAccountsCalculator() {
        return savingsAccountsCalculator;
    }
    
    /**
     * Получить LiveData с отформатированной общей суммой текущих счетов
     * Автоматически обновляется при изменении данных
     */
    public LiveData<String> getFormattedCurrentAccountsAmountLiveData() {
        MutableLiveData<String> formattedAmount = new MutableLiveData<>("0.00");
        
        if (currentAccountsCalculator != null) {
            // Подписываемся на изменения общей суммы и форматируем её
            currentAccountsCalculator.getResultAmount().observeForever(totalAmount -> {
                if (totalAmount != null) {
                    String formatted = formatter.formatFromCents(totalAmount);
                    formattedAmount.setValue(formatted);
                    Log.d(TAG, "Общая сумма текущих счетов обновлена: " + formatted);
                } else {
                    formattedAmount.setValue("0.00");
                }
            });
        }
        
        return formattedAmount;
    }
    
    /**
     * Получить LiveData с отформатированной общей суммой сберегательных счетов
     * Автоматически обновляется при изменении данных
     */
    public LiveData<String> getFormattedSavingsAccountsAmountLiveData() {
        MutableLiveData<String> formattedAmount = new MutableLiveData<>("0.00");
        
        if (savingsAccountsCalculator != null) {
            // Подписываемся на изменения общей суммы и форматируем её
            savingsAccountsCalculator.getResultAmount().observeForever(totalAmount -> {
                if (totalAmount != null) {
                    String formatted = formatter.formatFromCents(totalAmount);
                    formattedAmount.setValue(formatted);
                    Log.d(TAG, "Общая сумма сберегательных счетов обновлена: " + formatted);
                } else {
                    formattedAmount.setValue("0.00");
                }
            });
        }
        
        return formattedAmount;
    }
    
    
    /**
     * Устанавливает отображаемую валюту для всех калькуляторов счетов
     * @param currencyId ID валюты для отображения
     */
    public void setAccountsCalculatorDisplayCurrency(int currencyId) {
        if (currentAccountsCalculator != null) {
            currentAccountsCalculator.setDisplayCurrencyId(currencyId);
            Log.d(TAG, "Установлена отображаемая валюта для калькулятора текущих счетов: " + currencyId);
        }
        
        if (savingsAccountsCalculator != null) {
            savingsAccountsCalculator.setDisplayCurrencyId(currencyId);
            Log.d(TAG, "Установлена отображаемая валюта для калькулятора сберегательных счетов: " + currencyId);
        }
    }
}
