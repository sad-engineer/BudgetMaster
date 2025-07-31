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

/**
 * Экран главной страницы
 */
public class MainActivity extends BaseNavigationActivity {

    /**
     * Тег для логирования
     */
    private static final String TAG = "MainActivity";

    /**
     * Флаг активности MainActivity
     */
    private boolean isActivityActive = true;

    /**
     * Создает экран главной страницы
     * @param savedInstanceState - сохраненное состояние
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Room ORM автоматически инициализирует базу данных

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);

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
    }

    @Override
    protected void showInstructions() {
        if (isActivityActive) {
            Toast.makeText(this, "Инструкции", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void showStatistics() {
        if (isActivityActive) {
            Toast.makeText(this, "Статистика", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void showIncomeCategories() {
        if (isActivityActive) {
            Toast.makeText(this, "Категории доходов", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void showExpenseCategories() {
        if (isActivityActive) {
            Toast.makeText(this, "Категории расходов", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void showImportData() {
        if (isActivityActive) {
            Toast.makeText(this, "Загрузить данные", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void showExportData() {
        if (isActivityActive) {
            Toast.makeText(this, "Выгрузить данные", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void navigateToSettings() {
        if (isActivityActive) {
            Intent intent = new Intent(this, BackendTestActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🚀" + TAG + " запущена");
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        Log.d(TAG, "▶️" + TAG + " возобновлена");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        Log.d(TAG, "⏸️" + TAG + " приостановлена");
        // Останавливаем обновления UI когда приложение не активно
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        isActivityActive = false;
        Log.d(TAG, "🛑" + TAG + " остановлена");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        Log.d(TAG, "🛑" + TAG + " уничтожена");
        // Освобождаем ресурсы
    }
}