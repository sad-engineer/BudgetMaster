package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.accounts.Accounts;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.currencies.CurrenciesActivity;
import com.sadengineer.budgetmaster.settings.SettingsActivity;
import com.sadengineer.budgetmaster.VersionActivity;
import com.sadengineer.budgetmaster.AuthorsActivity;
import com.sadengineer.budgetmaster.BackendTestActivity;

public abstract class BaseNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    /**
     * Инициализация навигационного меню
     * Вызывается в onCreate() дочерних классов
     */
    protected void initializeNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Настройка кнопки меню для открытия бокового меню
     * @param menuButtonId ID кнопки меню в toolbar
     */
    protected void setupMenuButton(int menuButtonId) {
        ImageButton menuButton = findViewById(menuButtonId);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    /**
     * Настройка кнопки "назад" для возврата на главный экран
     * @param backButtonId ID кнопки назад в toolbar
     */
    protected void setupBackButton(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        // Обработка выбора пунктов меню
        if (id == R.id.nav_main) {
            navigateToMain();
        } else if (id == R.id.nav_instructions) {
            showInstructions();
        } else if (id == R.id.nav_currencies) {
            navigateToCurrencies();
        } else if (id == R.id.nav_accounts) {
            navigateToAccounts();
        } else if (id == R.id.nav_income) {
            navigateToIncome();
        } else if (id == R.id.nav_expense) {
            navigateToExpense();
        } else if (id == R.id.nav_budget) {
            navigateToBudget();
        } else if (id == R.id.nav_income_categories) {
            showIncomeCategories();
        } else if (id == R.id.nav_expense_categories) {
            showExpenseCategories();
        } else if (id == R.id.nav_import_data) {
            showImportData();
        } else if (id == R.id.nav_export_data) {
            showExportData();
        } else if (id == R.id.nav_settings) {
            navigateToSettings();
        } else if (id == R.id.nav_about) {
            navigateToVersion();
        } else if (id == R.id.nav_authors) {
            navigateToAuthors();
        } else if (id == R.id.nav_statistics) {
            showStatistics();
        } else if (id == R.id.nav_backend_test) {
            showBackendTest();
        }
        
        // Закрываем боковое меню
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Методы навигации - могут быть переопределены в дочерних классах
    protected void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    protected void navigateToCurrencies() {
        // Если уже на экране валют, просто закрываем меню
        if (this instanceof CurrenciesActivity) {
            return;
        }
        Intent intent = new Intent(this, CurrenciesActivity.class);
        startActivity(intent);
    }

    protected void navigateToAccounts() {
        Intent intent = new Intent(this, Accounts.class);
        startActivity(intent);
    }

    protected void navigateToIncome() {
        Intent intent = new Intent(this, IncomeActivity.class);
        startActivity(intent);
    }

    protected void navigateToExpense() {
        Intent intent = new Intent(this, ExpenseActivity.class);
        startActivity(intent);
    }

    protected void navigateToBudget() {
        Intent intent = new Intent(this, BudgetActivity.class);
        startActivity(intent);
    }

    protected void navigateToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    protected void navigateToVersion() {
        Intent intent = new Intent(this, VersionActivity.class);
        startActivity(intent);
    }

    protected void navigateToAuthors() {
        Intent intent = new Intent(this, AuthorsActivity.class);
        startActivity(intent);
    }

    // Методы для показа временных сообщений - могут быть переопределены
    protected void showInstructions() {
        // Показываем Toast или переходим на экран инструкций
    }

    protected void showStatistics() {
        // Показываем Toast или переходим на экран статистики
    }

    protected void showIncomeCategories() {
        // Показываем Toast или переходим на экран категорий доходов
    }

    protected void showExpenseCategories() {
        // Показываем Toast или переходим на экран категорий расходов
    }

    protected void showImportData() {
        // Показываем Toast или переходим на экран импорта данных
    }

    protected void showExportData() {
        // Показываем Toast или переходим на экран экспорта данных
    }

    protected void showBackendTest() {
        // Показываем Toast или переходим на экран теста Backend
    }
} 