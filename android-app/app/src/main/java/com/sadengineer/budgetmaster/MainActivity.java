package com.sadengineer.budgetmaster;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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


public class MainActivity extends BaseNavigationActivity {
    
    private static final String TAG = "MainActivity";
    private DatabaseManager databaseManager;

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
                goTo(AccountsActivity.class, 0); // 0 - Текущие
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
                goTo(BudgetActivity.class); //TODO: переделать на переход к  остатку бюджета
            }
        });
        
        Log.d(TAG, "MainActivity.onCreate() - инициализация завершена успешно");
    }
    
    /**
     * Уничтожение активности
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity уничтожена");
    }
    
    /**
     * Выход из приложения
     * Вызывается при нажатии кнопки "Выход" в меню
     */
    public void exitApplication() {
        Log.d(TAG, "Завершение работы приложения");
        // Завершаем ThreadManager
        ThreadManager.shutdown();
        // Закрываем приложение
        finish();
        System.exit(0);
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