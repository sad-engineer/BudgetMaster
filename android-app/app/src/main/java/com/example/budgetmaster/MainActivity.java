package com.example.budgetmaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.budgetmaster.accounts.Accounts;
import com.example.budgetmaster.income.IncomeActivity;
import com.example.budgetmaster.expense.ExpenseActivity;
import com.example.budgetmaster.budget.BudgetActivity;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Настройка drawer без ActionBarDrawerToggle
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // Анимация затемнения контента при открытии drawer
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // Drawer открыт
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // Drawer закрыт
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Состояние drawer изменилось
            }
        });

        navigationView.setNavigationItemSelectedListener(this);

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

        // Обработчики кнопок
        ImageButton menuButton = toolbar.findViewById(R.id.menu_button);
        ImageButton incomeButton = toolbar.findViewById(R.id.income_button);
        ImageButton expenseButton = toolbar.findViewById(R.id.expense_button);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Уже на главной странице
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_instructions) {
            Toast.makeText(this, "Инструкции", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_accounts) {
            Intent intent = new Intent(this, Accounts.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_income) {
            Intent intent = new Intent(this, IncomeActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_expense) {
            Intent intent = new Intent(this, ExpenseActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_budget) {
            Intent intent = new Intent(this, BudgetActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_statistics) {
            Toast.makeText(this, "Статистика", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_currencies) {
            Intent intent = new Intent(this, com.example.budgetmaster.currencies.CurrenciesActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_income_categories) {
            Toast.makeText(this, "Категории доходов", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_expense_categories) {
            Toast.makeText(this, "Категории расходов", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_import_data) {
            Toast.makeText(this, "Загрузить данные", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_export_data) {
            Toast.makeText(this, "Выгрузить данные", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, BackendTestActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_version) {
            Toast.makeText(this, "Версия приложения", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_authors) {
            Toast.makeText(this, "Авторы", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}