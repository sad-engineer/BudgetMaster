package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.util.Log;
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

    private static final String TAG = "BaseNavigationActivity";

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
        Log.d(TAG, "✅ Навигация инициализирована");
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
                    Log.d(TAG, "👆 Открыто боковое меню");
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
                Log.d(TAG, "👆 Нажата кнопка назад");
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Обработка выбора пунктов меню
     * @param item - выбранный пункт меню
     * @return true, если обработка выполнена успешно
     */
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

    /**
     * Обработка нажатия кнопки "назад"
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            Log.d(TAG, "👆 Закрыто боковое меню");
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Переход на главный экран
     */
    protected void navigateToMain() {
        // Если уже на главном экране, просто закрываем меню
        if (this instanceof MainActivity) {
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Log.d(TAG, "Переход на главный экран");
    }

    /**
     * Переход на экран валют
     */
    protected void navigateToCurrencies() {
        // Если уже на экране валют, просто закрываем меню
        if (this instanceof CurrenciesActivity) {
            return;
        }
        Intent intent = new Intent(this, CurrenciesActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран валют");
    }

    /**
     * Переход на экран счетов
     */
    protected void navigateToAccounts() {
        // Если уже на экране счетов, просто закрываем меню
        if (this instanceof Accounts) {
            return;
        }
        Intent intent = new Intent(this, Accounts.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран счетов");
    }

    /**
     * Переход на экран доходов
     */
    protected void navigateToIncome() {
        // Если уже на экране доходов, просто закрываем меню
        if (this instanceof IncomeActivity) {
            return;
        }
        Intent intent = new Intent(this, IncomeActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран доходов");
    }

    /**
     * Переход на экран расходов
     */
    protected void navigateToExpense() {
        // Если уже на экране расходов, просто закрываем меню
        if (this instanceof ExpenseActivity) {
            return;
        }
        Intent intent = new Intent(this, ExpenseActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран расходов");
    }

    /**
     * Переход на экран бюджета
     */
    protected void navigateToBudget() {
        // Если уже на экране бюджета, просто закрываем меню
        if (this instanceof BudgetActivity) {
            return;
        }
        Intent intent = new Intent(this, BudgetActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран бюджета");
    }



    /**
     * Переход на экран настроек
     */
    protected void navigateToSettings() {
        // Если уже на экране настроек, просто закрываем меню
        if (this instanceof SettingsActivity) {
            return;
        }
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран настроек");
    }

    /**
     * Переход на экран версии
     */
    protected void navigateToVersion() {
        // Если уже на экране версии, просто закрываем меню
        if (this instanceof VersionActivity) {
            return;
        }
        Intent intent = new Intent(this, VersionActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран версии");
    }

    /**
     * Переход на экран авторов
     */
    protected void navigateToAuthors() {
        // Если уже на экране авторов, просто закрываем меню
        if (this instanceof AuthorsActivity) {
            return;
        }
        Intent intent = new Intent(this, AuthorsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран авторов");
    }

    // Методы для показа временных сообщений - могут быть переопределены
    protected void showInstructions() {
        // Показываем Toast или переходим на экран инструкций
        Log.d(TAG, "Показать инструкции");
    }

    protected void showStatistics() {
        // Показываем Toast или переходим на экран статистики
        Log.d(TAG, "Показать статистику");
    }

    protected void showIncomeCategories() {
        // Показываем Toast или переходим на экран категорий доходов
        Log.d(TAG, "Показать категории доходов");
    }

    protected void showExpenseCategories() {
        // Показываем Toast или переходим на экран категорий расходов
        Log.d(TAG, "Показать категории расходов");
    }

    protected void showImportData() {
        // Показываем Toast или переходим на экран импорта данных
        Log.d(TAG, "Показать импорт данных");
    }

    protected void showExportData() {
        // Показываем Toast или переходим на экран экспорта данных
        Log.d(TAG, "Показать экспорт данных");
    }

    protected void showBackendTest() {
        // Показываем Toast или переходим на экран теста Backend
        Log.d(TAG, "Показать тест Backend");
    }
} 