package com.sadengineer.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.sadengineer.budgetmaster.accounts.Accounts;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.database.DatabaseManager;


public class MainActivity extends BaseNavigationActivity {
    
    private static final String TAG = "MainActivity";
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🚀 MainActivity.onCreate() - начало инициализации");
        
        setContentView(R.layout.activity_main);
        Log.d(TAG, "📱 Layout загружен");

        // Инициализация базы данных
        Log.d(TAG, "🔧 Начинаем инициализацию базы данных");
        initializeDatabase();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "🔧 Toolbar настроен");

        // Инициализация навигации
        Log.d(TAG, "🔧 Инициализация навигации");
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        Log.d(TAG, "🔧 Навигация настроена");

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
        Log.d(TAG, "🔧 Обработчик кнопки 'Назад' настроен");

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
        Log.d(TAG, "🔧 Кнопки toolbar настроены");

        // Обработчик кнопки "На счетах"
        Button btnAccounts = findViewById(R.id.btn_accounts);
        btnAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Accounts.class);
                intent.putExtra("tab_index", 0); // 0 - Текущие
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Заработанно за месяц" (открывает вкладку Текущие)
        Button btnEarned = findViewById(R.id.btn_earned);
        btnEarned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Accounts.class);
                intent.putExtra("tab_index", 0); // 0 - Текущие
                startActivity(intent);
            }
        });

        // Обработчик кнопки "Сбережения" (открывает вкладку Сбережения)
        Button btnSavings = findViewById(R.id.btn_savings);
        btnSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Accounts.class);
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
        
        Log.d(TAG, "✅ MainActivity.onCreate() - инициализация завершена успешно");
    }

    // Переопределяем методы для показа Toast сообщений
    @Override
    protected void showInstructions() {
        Toast.makeText(this, "Инструкции", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showStatistics() {
        Toast.makeText(this, "Статистика", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showIncomeCategories() {
        Toast.makeText(this, "Категории доходов", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showExpenseCategories() {
        Toast.makeText(this, "Категории расходов", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showImportData() {
        Toast.makeText(this, "Загрузить данные", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void showExportData() {
        Toast.makeText(this, "Выгрузить данные", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void navigateToSettings() {
        Intent intent = new Intent(this, BackendTestActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🟢 MainActivity.onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🟢 MainActivity.onResume()");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "🟡 MainActivity.onPause()");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "🟡 MainActivity.onStop()");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🔴 MainActivity.onDestroy()");
    }

    /**
     * Инициализирует базу данных
     */
    private void initializeDatabase() {
        try {
            Log.d(TAG, "🔧 Инициализация базы данных...");
            databaseManager = new DatabaseManager(this);
            
            // Инициализируем базу данных асинхронно
            databaseManager.initializeDatabase().thenAccept(success -> {
                if (success) {
                    Log.d(TAG, "✅ База данных инициализирована успешно");
                } else {
                    Log.e(TAG, "❌ Ошибка инициализации базы данных");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_LONG).show();
                    });
                }
            }).exceptionally(throwable -> {
                Log.e(TAG, "❌ Исключение при инициализации БД: " + throwable.getMessage(), throwable);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка инициализации БД: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка создания DatabaseManager: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка создания DatabaseManager: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}