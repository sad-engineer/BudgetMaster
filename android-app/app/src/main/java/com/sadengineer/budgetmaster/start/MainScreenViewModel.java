package com.sadengineer.budgetmaster.start;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sadengineer.budgetmaster.start.MainScreenData;
import com.sadengineer.budgetmaster.start.MainScreenRepository;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.R;

/**
 * ViewModel для главного экрана
 * Управляет данными и бизнес-логикой главного экрана
 */
public class MainScreenViewModel extends AndroidViewModel {
    private static final String TAG = "MainScreenViewModel";
    
    private final MainScreenRepository repository;
    private final MutableLiveData<Boolean> isRefreshing;
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    public MainScreenViewModel(@NonNull Application application) {
        super(application);
        
        this.repository = new MainScreenRepository(application, userName);
        this.isRefreshing = new MutableLiveData<>(false);
        
        Log.d(TAG, "MainScreenViewModel инициализирован");
    }
    
    /**
     * Получить данные главного экрана
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
        new android.os.Handler().postDelayed(() -> {
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
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getTotalAccountsBalance());
    }
    
    public String getFormattedMonthlyEarned() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getMonthlyEarned());
    }
    
    public String getFormattedTotalSavingsBalance() {
        MainScreenData data = getMainScreenData().getValue();
        if (data == null) return "0.00";
        return formatter.formatFromCents(data.getTotalSavingsBalance());
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
    
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MainScreenViewModel очищен");
    }
}
