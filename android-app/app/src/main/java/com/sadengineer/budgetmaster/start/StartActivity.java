package com.sadengineer.budgetmaster.start;

import android.os.Bundle;
 
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sadengineer.budgetmaster.accounts.AccountsActivity;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.database.DatabaseManager;
import com.sadengineer.budgetmaster.settings.SettingsManager;
import com.sadengineer.budgetmaster.backend.service.ServiceManager;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.settings.AppSettings;
import com.sadengineer.budgetmaster.utils.LogManager;

public class StartActivity extends BaseNavigationActivity {
    
    private static final String TAG = "StartActivity";
    private DatabaseManager databaseManager;
    private ServiceManager serviceManager;
    private StartScreenViewModel viewModel;
    private AppSettings appSettings;
    
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
        LogManager.d(TAG, "StartActivity.onCreate() - начало инициализации");
        setContentView(R.layout.activity_main);
        // Инициализация базы данных
        initializeDatabase();
        // Инициализация настроек
        SettingsManager.init(this);
        appSettings = new AppSettings(this);
        // Инициализация навигации
        initializeNavigation(true);
        // Инициализация менеджера сервисов
        serviceManager = ServiceManager.getInstance(this, userName);
        
        // Инициализация ViewModel
        viewModel = new StartScreenViewModel(getApplication());
        
        // Настройка отображаемой валюты для калькуляторов
        viewModel.setBudgetCalculatorDisplayCurrency(appSettings.getDefaultCurrencyId());
        viewModel.setAccountsCalculatorDisplayCurrency(appSettings.getDefaultCurrencyId());
        viewModel.setOperationsCalculatorDisplayCurrency(appSettings.getDefaultCurrencyId());
        
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
                LogManager.d(TAG, "Касание поля суммы бюджетов - принудительный пересчет");
                viewModel.forceRecalculateBudgets();
            }
        });
        
        LogManager.d(TAG, "StartActivity.onCreate() - инициализация завершена успешно");
    }
    
    /**
     * Настройка наблюдателей LiveData
     */
    private void setupObservers() {
        // Наблюдаем за данными стартового экрана
        viewModel.getMainScreenData().observe(this, data -> {
            if (data != null) {
                updateUI(data);
                LogManager.d(TAG, "Данные стартового экрана обновлены: " + data);
            }
        });
        
        // Наблюдаем за состоянием загрузки
        viewModel.getIsLoading().observe(this, isLoading -> {
            // TODO: показать/скрыть индикатор загрузки
            LogManager.d(TAG, "Состояние загрузки: " + isLoading);
        });
        
        // Наблюдаем за ошибками
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // TODO: показать сообщение об ошибке
                LogManager.e(TAG, "Ошибка: " + error);
            }
        });
        
        // Наблюдаем за общей суммой бюджетов для отладки
        viewModel.getTotalBudgetAmount().observe(this, totalAmount -> {
            if (totalAmount != null) {
                valueBudget.setText(viewModel.getFormattedTotalBudgetAmount());
                valueBudget.setTextColor(viewModel.getBudgetRemainingColor());
                LogManager.d(TAG, "Общая сумма бюджетов обновлена: " + totalAmount);
            }
        });
        
        // Наблюдаем за суммой текущих счетов
        viewModel.getCurrentTotalAccountsAmount().observe(this, totalAmount -> {
            if (totalAmount != null) {
                valueAccounts.setText(viewModel.getFormattedTotalAccountsBalance());
                valueAccounts.setTextColor(viewModel.getAmountColor(totalAmount));
                LogManager.d(TAG, "Сумма текущих счетов обновлена: " + totalAmount);
            }
        });
        
        // Наблюдаем за суммой сберегательных счетов
        viewModel.getSavingsAccountsCalculator().getResultAmount().observe(this, totalAmount -> {
            if (totalAmount != null) {
                valueSavings.setText(viewModel.getFormattedTotalSavingsBalance());
                valueSavings.setTextColor(viewModel.getAmountColor(totalAmount));
                LogManager.d(TAG, "Сумма сберегательных счетов обновлена: " + totalAmount);
            }
        });
        
        // Наблюдаем за суммой месячного дохода
        viewModel.getMonthlyEarnedCalculator().getResultAmount().observe(this, totalAmount -> {
            if (totalAmount != null) {
                valueEarned.setText(viewModel.getFormattedMonthlyEarned());
                valueEarned.setTextColor(viewModel.getAmountColor(totalAmount));
                LogManager.d(TAG, "Сумма месячного дохода обновлена: " + totalAmount);
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
        LogManager.d(TAG, "Обновление UI с данными: " + data);
        
        // Обновляем текстовые поля с форматированными значениями
        valueEarned.setText(viewModel.getFormattedMonthlyEarned());
        valueReserve.setText(viewModel.getFormattedReserveAmount());
        
        // Устанавливаем цвета для сумм
        valueReserve.setTextColor(viewModel.getAmountColor(data.getReserveAmount()));
        valueEarned.setTextColor(viewModel.getAmountColor(data.getMonthlyEarned()));
        
        LogManager.d(TAG, "UI успешно обновлен");
    }
            
    /**
     * Возобновление активности
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возврате на стартовый экран
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
        LogManager.d(TAG, "StartActivity уничтожена");
    }
    
    /**
     * Инициализирует базу данных
     * TODO: убрать логику инициализации базы данных из StartActivity, только вызов
     */
    private void initializeDatabase() {
        try {
            LogManager.d(TAG, "Инициализация базы данных...");
            databaseManager = new DatabaseManager(this);
            
            // Инициализируем базу данных асинхронно
            databaseManager.initializeDatabase().thenAccept(success -> {
                if (success) {
                    LogManager.d(TAG, "База данных инициализирована успешно");
                } else {
                    LogManager.e(TAG, "Ошибка инициализации базы данных");
                }
            }).exceptionally(throwable -> {
                LogManager.e(TAG, "Исключение при инициализации БД: " + throwable.getMessage(), throwable);
                return null;
            });
            
        } catch (Exception e) {
            LogManager.e(TAG, "Ошибка создания DatabaseManager: " + e.getMessage(), e);
        }
    }
}
