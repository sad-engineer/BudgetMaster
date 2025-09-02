package com.sadengineer.budgetmaster.start;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.GravityCompat;

import com.sadengineer.budgetmaster.accounts.AccountsActivity;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.database.DatabaseManager;
import com.sadengineer.budgetmaster.settings.SettingsManager;
import com.sadengineer.budgetmaster.backend.ThreadManager;
import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.start.MainScreenViewModel;
import com.sadengineer.budgetmaster.start.MainScreenData;
import com.sadengineer.budgetmaster.R;


public class MainActivity extends BaseNavigationActivity {
    
    private static final String TAG = "MainActivity";
    private DatabaseManager databaseManager;
    private ServiceManager serviceManager;
    private MainScreenViewModel viewModel;
    
    // UI элементы для отображения данных
    private TextView valueEarned;
    private TextView valueAccounts;
    private TextView valueSavings;
    private TextView valueBudget;
    private TextView valueReserve;

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity.onCreate() - начало инициализации");
        setContentView(R.layout.activity_main);
        // Инициализация базы данных
        initializeDatabase();
        // Инициализация настроек
        SettingsManager.init(this);
        // Инициализация навигации
        initializeNavigation(true);
        // Инициализация менеджера сервисов
        serviceManager = ServiceManager.getInstance(this, userName);
        
        // Инициализация ViewModel
        viewModel = new MainScreenViewModel(getApplication());
        
        // Настройка наблюдателей LiveData
        setupObservers();
        
        // Инициализация UI элементов
        initializeUIElements();
        
        // Загрузка данных
        viewModel.refreshData();
        
        // ====== Обработчики кнопок ======

        // Обработчик кнопки "На счетах"
        Button btnAccounts = findViewById(R.id.btn_accounts);
        btnAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(AccountsActivity.class, 0); // 0 - Текущие
            }
        });

        // Обработчик кнопки "Заработанно за месяц" (открывает вкладку Текущие)
        Button btnEarned = findViewById(R.id.btn_earned);
        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(IncomeActivity.class, 0); // 0 - Текущие
            }
        });

        // Обработчик кнопки "Сбережения" (открывает вкладку Сбережения)
        Button btnSavings = findViewById(R.id.btn_savings);
        btnSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(AccountsActivity.class, 1); // 1 - Сбережения
            }
        });

        // Обработчик кнопки "Внести доход" (открывает экран доходов)
        Button btnIncome = findViewById(R.id.btn_income);
        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(IncomeActivity.class);
            }
        });

        // Обработчик кнопки "Внести расход" (открывает экран расходов)
        Button btnExpense = findViewById(R.id.btn_expense);
        btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(ExpenseActivity.class);
            }
        });

        // Обработчик кнопки "Остаток бюджета" (открывает экран бюджета)
        Button btnBudget = findViewById(R.id.btn_budget);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(BudgetActivity.class); 
            }
        });
        
        // Добавляем пересчет бюджетов при касании поля суммы
        valueBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Касание поля суммы бюджетов - принудительный пересчет");
                viewModel.forceRecalculateBudgets();
            }
        });
        
        Log.d(TAG, "MainActivity.onCreate() - инициализация завершена успешно");
    }
    
    /**
     * Настройка наблюдателей LiveData
     */
    private void setupObservers() {
        // Наблюдаем за данными главного экрана
        viewModel.getMainScreenData().observe(this, data -> {
            if (data != null) {
                updateUI(data);
                Log.d(TAG, "Данные главного экрана обновлены: " + data);
            }
        });
        
        // Наблюдаем за состоянием загрузки
        viewModel.getIsLoading().observe(this, isLoading -> {
            // TODO: показать/скрыть индикатор загрузки
            Log.d(TAG, "Состояние загрузки: " + isLoading);
        });
        
        // Наблюдаем за ошибками
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // TODO: показать сообщение об ошибке
                Log.e(TAG, "Ошибка: " + error);
            }
        });
        
        // Наблюдаем за общей суммой бюджетов для отладки
        viewModel.getTotalBudgetAmount().observe(this, totalAmount -> {
            if (totalAmount != null) {
                valueBudget.setText(viewModel.getFormattedTotalBudgetAmount());
                Log.d(TAG, "Общая сумма бюджетов обновлена: " + totalAmount);
            }
        });
    }
    
    /**
     * Инициализация UI элементов
     */
    private void initializeUIElements() {
        valueEarned = findViewById(R.id.value_earned);
        valueAccounts = findViewById(R.id.value_accounts);
        valueSavings = findViewById(R.id.value_savings);
        valueBudget = findViewById(R.id.value_budget);
        valueReserve = findViewById(R.id.value_reserve);
    }
    
    /**
     * Обновление UI с новыми данными
     */
    private void updateUI(MainScreenData data) {
        Log.d(TAG, "Обновление UI с данными: " + data);
        
        // Обновляем текстовые поля с форматированными значениями
        valueEarned.setText(viewModel.getFormattedMonthlyEarned());
        valueAccounts.setText(viewModel.getFormattedTotalAccountsBalance());
        valueSavings.setText(viewModel.getFormattedTotalSavingsBalance());
        valueBudget.setText(viewModel.getFormattedTotalBudgetAmount());
        valueReserve.setText(viewModel.getFormattedReserveAmount());
        
        // Устанавливаем цвета для сумм
        valueAccounts.setTextColor(viewModel.getAmountColor(data.getTotalAccountsBalance()));
        valueSavings.setTextColor(viewModel.getAmountColor(data.getTotalSavingsBalance()));
        valueBudget.setTextColor(viewModel.getBudgetRemainingColor());
        valueReserve.setTextColor(viewModel.getAmountColor(data.getReserveAmount()));
        valueEarned.setTextColor(viewModel.getAmountColor(data.getMonthlyEarned()));
        
        Log.d(TAG, "UI успешно обновлен");
    }
            
    /**
     * Возобновление активности
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возврате на главный экран
        if (viewModel != null) {
            viewModel.onResume();
        }
    }
    
    /**
     * Уничтожение активности
     */
    @Override
    protected void onDestroy() {
        // НЕ завершаем ThreadManager здесь, так как он может понадобиться другим компонентам
        // ThreadManager.shutdown() должен вызываться только при завершении всего приложения
        
        super.onDestroy();
        Log.d(TAG, "MainActivity уничтожена");
    }
    
    /**
     * Инициализирует базу данных
     * TODO: убрать логику инициализации базы данных из MainActivity, только вызов
     */
    private void initializeDatabase() {
        try {
            Log.d(TAG, "Инициализация базы данных...");
            databaseManager = new DatabaseManager(this);
            
            // Инициализируем базу данных асинхронно
            databaseManager.initializeDatabase().thenAccept(success -> {
                if (success) {
                    Log.d(TAG, "База данных инициализирована успешно");
                } else {
                    Log.e(TAG, "Ошибка инициализации базы данных");
                }
            }).exceptionally(throwable -> {
                Log.e(TAG, "Исключение при инициализации БД: " + throwable.getMessage(), throwable);
                return null;
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка создания DatabaseManager: " + e.getMessage(), e);
        }
    }
}
