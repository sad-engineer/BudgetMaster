package com.sadengineer.budgetmaster.navigation;

import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import com.sadengineer.budgetmaster.MainActivity;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.accounts.AccountsActivity;
import com.sadengineer.budgetmaster.income.IncomeActivity;
import com.sadengineer.budgetmaster.expense.ExpenseActivity;
import com.sadengineer.budgetmaster.budget.BudgetActivity;
import com.sadengineer.budgetmaster.currencies.CurrenciesActivity;
import com.sadengineer.budgetmaster.settings.SettingsActivity;
import com.sadengineer.budgetmaster.VersionActivity;
import com.sadengineer.budgetmaster.AuthorsActivity;
import com.sadengineer.budgetmaster.BackendTestActivity;

/**
 * Базовый класс для всех Activity с навигацией
 * Содержит методы для инициализации навигации, обработки свайпов и переходов между экранами
 */
public abstract class BaseNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BaseNavigationActivity";

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    private SwipeNavigationHelper swipeNavigationHelper;

    /**
     * Инициализация навигационного меню
     * Вызывается в onCreate() дочерних классов
     */
    protected void initializeNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        
        // Инициализируем помощник для свайпов
        swipeNavigationHelper = new SwipeNavigationHelper(this);
        
        // Добавляем слушатель состояния меню
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(new androidx.drawerlayout.widget.DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull android.view.View drawerView, float slideOffset) {
                    // Не нужно ничего делать при скольжении
                }

                @Override
                public void onDrawerOpened(@NonNull android.view.View drawerView) {
                    // Отключаем свайпы при открытии меню
                    if (swipeNavigationHelper != null) {
                        swipeNavigationHelper.setEnabled(false);
                    }
                    Log.d(TAG, "📱 Меню открыто - свайпы отключены");
                }

                @Override
                public void onDrawerClosed(@NonNull android.view.View drawerView) {
                    // Включаем свайпы при закрытии меню
                    if (swipeNavigationHelper != null) {
                        swipeNavigationHelper.setEnabled(true);
                    }
                    Log.d(TAG, "📱 Меню закрыто - свайпы включены");
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    // Не нужно ничего делать при изменении состояния
                }
            });
        }
        
        Log.d(TAG, "✅ Навигация инициализирована");
    }

    /**
     * Обработка касаний экрана для свайпов
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeNavigationHelper != null) {
            return swipeNavigationHelper.onTouchEvent(event) || super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    /**
     * Обработка касаний для всех дочерних View
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (swipeNavigationHelper != null) {
            boolean handled = swipeNavigationHelper.onTouchEvent(event);
            if (handled) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Сбрасывает счетчик свайпов (вызывается при изменении содержимого списков)
     */
    public void resetSwipeCount() {
        if (swipeNavigationHelper != null) {
            swipeNavigationHelper.resetSwipeCount();
        }
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
                    Log.d(TAG, "👆 Нажата кнопка меню");
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
                Log.d(TAG, "👆 Нажата кнопка назад");
            });
        }
    }

    /**
     * Комплексная инициализация стандартного тулбара по фиксированным ID
     * back_button, menu_button, toolbar_title, position_change_button.
     * Любой из элементов может отсутствовать в конкретном layout.
     */
    protected void setupStandardToolbar() {
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        // Кнопка смены позиции — просто логируем клик по умолчанию
        ImageButton positionButton = findViewById(R.id.position_change_button);
        if (positionButton != null) {
            positionButton.setOnClickListener(v -> Log.d(TAG, "👆 Нажата кнопка смены позиции"));
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
        if (this instanceof AccountsActivity) {
            return;
        }
        Intent intent = new Intent(this, AccountsActivity.class);
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

    // не реализованные экраны
    // TODO: реализовать экраны

    /**
     * Показывает экран инструкций
     */
    protected void showInstructions() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.instructions.InstructionsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран инструкций");
    }

    /**
     * Показывает экран статистики
     */
    protected void showStatistics() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.statistics.StatisticsActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран статистики");
    }

    /**
     * Показывает экран категорий доходов
     */
    protected void showIncomeCategories() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.categories.IncomeCategoriesActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран категорий доходов");
    }

    /**
     * Показывает экран категорий расходов
     */
    protected void showExpenseCategories() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.categories.ExpenseCategoriesActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран категорий расходов");
    }

    /**
     * Показывает экран импорта данных
     */
    protected void showImportData() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.import_export.ImportDataActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран импорта данных");
    }

    /**
     * Показывает экран экспорта данных
     */
    protected void showExportData() {
        Intent intent = new Intent(this, com.sadengineer.budgetmaster.import_export.ExportDataActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран экспорта данных");
    }

    /**
     * Показывает экран теста Backend
     */
    protected void showBackendTest() {
        Intent intent = new Intent(this, BackendTestActivity.class);
        startActivity(intent);
        Log.d(TAG, "Переход на экран теста Backend");
    }

    // Жизненный цикл Activity

    /**
     * Запуск Activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🚀 " + getClass().getSimpleName() + " запущена");
    }

    /**
     * Возобновление Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ " + getClass().getSimpleName() + " возобновлена");
        
        // Сбрасываем счетчик свайпов при возврате на экран
        resetSwipeCount();
    }

    /**
     * Приостановка Activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ " + getClass().getSimpleName() + " приостановлена");
    }

    /**
     * Остановка Activity
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "🛑 " + getClass().getSimpleName() + " остановлена");
    }

    /**
     * Уничтожение Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💀 " + getClass().getSimpleName() + " уничтожена");
    }
} 