package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
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
        Log.d(TAG, "Layout загружен");

        // Инициализация базы данных
        Log.d(TAG, "Начинаем инициализацию базы данных");
        initializeDatabase();

        // Инициализация настроек
        Log.d(TAG, "Инициализация настроек приложения");
        SettingsManager.init(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "Toolbar настроен");

        // Инициализация навигации
        Log.d(TAG, "Инициализация навигации");
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        
        // Устанавливаем targetRecyclerView в null (в MainActivity нет списка)
        setSwipeTargetRecyclerView(null);
        
        Log.d(TAG, "Навигация настроена");

        // Настройка обработки кнопки "Назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
        Log.d(TAG, "Обработчик кнопки 'Назад' настроен");

        // Обработчики кнопок toolbar
        ImageButton incomeButton = toolbar.findViewById(R.id.income_button);
        ImageButton expenseButton = toolbar.findViewById(R.id.expense_button);
        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
                startActivity(intent);
            }
        });
        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
                startActivity(intent);
            }
        });
        Log.d(TAG, "Кнопки toolbar настроены");

        // Обработчик кнопки "На счетах"
        Button btnAccounts = findViewById(R.id.btn_accounts);
        btnAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountsActivity.class);
                intent.putExtra("tab_index", 0); // 0 - Текущие
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Заработанно за месяц" (открывает вкладку Текущие)
        Button btnEarned = findViewById(R.id.btn_earned);
        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountsActivity.class);
                intent.putExtra("tab_index", 0); // 0 - Текущие
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Сбережения" (открывает вкладку Сбережения)
        Button btnSavings = findViewById(R.id.btn_savings);
        btnSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountsActivity.class);
                intent.putExtra("tab_index", 1); // 1 - Сбережения
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Внести доход" (открывает экран доходов)
        Button btnIncome = findViewById(R.id.btn_income);
        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, IncomeActivity.class);
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Внести расход" (открывает экран расходов)
        Button btnExpense = findViewById(R.id.btn_expense);
        btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Остаток бюджета" (открывает экран бюджета)
        Button btnBudget = findViewById(R.id.btn_budget);
        btnBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BudgetActivity.class);
                startActivity(intent);
            }
        });
        
        Log.d(TAG, "MainActivity.onCreate() - инициализация завершена успешно");
    }
    
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