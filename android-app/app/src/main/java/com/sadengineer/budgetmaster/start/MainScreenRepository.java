package com.sadengineer.budgetmaster.start;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.start.MainScreenData;
import com.sadengineer.budgetmaster.backend.filters.AccountTypeFilter;
import com.sadengineer.budgetmaster.backend.service.AccountService;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.OperationService;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Repository для агрегации данных главного экрана
 * Собирает данные из разных сервисов и предоставляет единый интерфейс
 */
public class MainScreenRepository {
    private static final String TAG = "MainScreenRepository";
    
    // Константы для расчета резерва
    private static final int MONTHS_FOR_EXPENSE_CALCULATION = 6;  // Количество месяцев для расчета средних трат
    private static final int MONTHS_FOR_RESERVE_CALCULATION = 9;  // Количество месяцев для резерва (подушка безопасности)
    
    private final ServiceManager sm;
    private final MutableLiveData<MainScreenData> data;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    public MainScreenRepository(Context context, String userName) {
        this.sm = ServiceManager.getInstance(context, userName);
        this.data = new MutableLiveData<>(new MainScreenData());
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        
        Log.d(TAG, "MainScreenRepository инициализирован для пользователя: " + userName);
    }
    
    /**
     * Получить LiveData с данными главного экрана
     */
    public LiveData<MainScreenData> getMainScreenData() {
        return data;
    }
    
    /**
     * Получить LiveData состояния загрузки
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Получить LiveData с сообщением об ошибке
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Обновить все данные главного экрана
     * Сервисы сами управляют своими потоками выполнения
     */
    public void refreshData() {
        Log.d(TAG, "Начинаем обновление данных главного экрана");
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // Используем ThreadManager напрямую, без дублирования
        ThreadManager.getExecutor().execute(() -> {
            try {
                MainScreenData newData = loadAllData();
                data.postValue(newData);
                Log.d(TAG, "Данные главного экрана обновлены: " + newData);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении данных: " + e.getMessage(), e);
                errorMessage.postValue("Ошибка загрузки данных: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
    
    /**
     * Загрузить все данные из сервисов
     */
    private MainScreenData loadAllData() {
        MainScreenData data = new MainScreenData();
        
        // 1. Общая сумма на счетах
        data.setTotalAccountsBalance(loadTotalAccountsBalance());
        
        // 2. Заработано за месяц
        data.setMonthlyEarned(loadMonthlyEarned());
        
        // 3. Общая сумма сбережений
        data.setTotalSavingsBalance(loadTotalSavingsBalance());
        
        // 4. Общий остаток бюджета
        data.setTotalBudgetRemaining(loadTotalBudgetRemaining());
        
        // 5. Сумма резерва
        data.setReserveAmount(getReserveAmount());
        
        return data;
    }
    
    /**
     * Загрузить общую сумму на текущих активных счетах
     * Если есть счета с разными валютами, то суммируем балансы в главной валюте (в валюте для отображения)
     */
    private long loadTotalAccountsBalance() {
        try {
            // Получаем все аккаунты и суммируем балансы
            AccountService service = sm.getAccountService();
            int type = AccountTypeFilter.CURRENT.getIndex();
            long amount = service.getTotalAmountByType(type, EntityFilter.ACTIVE);
            return amount;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки общей суммы на счетах: " + e.getMessage(), e);
            throw new RuntimeException (e);
        }
    }
    
    /**
     * Загрузить заработанное за месяц
     */
    private long loadMonthlyEarned() {
        try {
            OperationService service = sm.getOperationService();
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            long amount = service.getIncomeSumByDateRange(startOfMonth, endOfMonth);
            return amount;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки заработанного за месяц: " + e.getMessage(), e);
            throw new RuntimeException (e);
        }
    }
    
    /**
     * Загрузить общую сумму сбережений
     */
    private long loadTotalSavingsBalance() {
        try {
            // Получаем аккаунты типа "Сбережения" и суммируем балансы
            // TODO: сделать перевод сумм в основную валюту по заданному курсу
            AccountService service = sm.getAccountService();
            long amount = service.getTotalAmountByType(AccountTypeFilter.SAVINGS);
            return amount;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки суммы сбережений: " + e.getMessage(), e);
            throw new RuntimeException (e);
        }
    }
    
    /**
     * Загрузить общий остаток бюджета
     */
    private long loadTotalBudgetRemaining() {
        try {
            // Получаем сумму всех бюджетов
            BudgetService bs = sm.getBudgetService();
            long BudgetAmount = bs.getTotalAmount();

            //получаем сумму всех операций за месяц
            OperationService os = sm.getOperationService();
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            long OperationAmount = os.getExpenseSumByDateRange(startOfMonth, endOfMonth);

            //получаем остаток бюджета
            long RemainingAmount = BudgetAmount - OperationAmount;
            return RemainingAmount;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения остатка бюджета: " + e.getMessage(), e);
            throw new RuntimeException (e);
        }
    }
    
    /**
     * Загрузить сумму резерва
     * Сумма резерва равна MONTHS_FOR_RESERVE_CALCULATION средним месячным тратам
     * Рассчитывается из средних трат за MONTHS_FOR_EXPENSE_CALCULATION месяцев
     */
    private long getReserveAmount() {
        try {
            // Получаем сумму всех трат за MONTHS_FOR_EXPENSE_CALCULATION месяцев
            OperationService os = sm.getOperationService();
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startOfMonth = currentMonth.minusMonths(MONTHS_FOR_EXPENSE_CALCULATION).atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            long expenseAmount = os.getExpenseSumByDateRange(startOfMonth, endOfMonth);

            // Средний месячный остаток бюджета округленный до целого
            long averageMonthlyExpense = expenseAmount / MONTHS_FOR_EXPENSE_CALCULATION;
            
            // Сумма резерва (подушка безопасности)
            long reserveAmount = averageMonthlyExpense * MONTHS_FOR_RESERVE_CALCULATION;
            return reserveAmount;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки суммы резерва: " + e.getMessage(), e);
            throw new RuntimeException (e);
        }
    }
    
    /**
     * Обновить конкретное поле данных
     */
    public void updateField(String fieldName, long value) {
        MainScreenData currentData = data.getValue();
        if (currentData == null) return;
        
        MainScreenData newData = currentData.copy();
        
        switch (fieldName) {
            case "totalAccountsBalance":
                newData.setTotalAccountsBalance(value);
                break;
            case "monthlyEarned":
                newData.setMonthlyEarned(value);
                break;
            case "totalSavingsBalance":
                newData.setTotalSavingsBalance(value);
                break;
            case "totalBudgetRemaining":
                newData.setTotalBudgetRemaining(value);
                break;
            case "reserveAmount":
                newData.setReserveAmount(value);
                break;
            default:
                Log.w(TAG, "Неизвестное поле для обновления: " + fieldName);
                return;
        }
        
        data.setValue(newData);
        Log.d(TAG, "Обновлено поле " + fieldName + " = " + value);
    }
}
