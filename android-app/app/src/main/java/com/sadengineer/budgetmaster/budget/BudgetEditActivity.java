package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.BudgetService;
import com.sadengineer.budgetmaster.backend.service.CategoryService;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Category;
import com.sadengineer.budgetmaster.backend.entity.Currency;

import java.util.List;
import java.util.ArrayList;

/**
 * Activity для создания/изменения бюджета
 */
public class BudgetEditActivity extends BaseEditActivity<Budget> {
    
    private static final String TAG = "BudgetEditActivity";
    
    private EditText budgetAmountEdit;
    private Spinner budgetCategorySpinner;
    private Spinner budgetCurrencySpinner;
    private BudgetService budgetService;
    private CategoryService categoryService;
    private CurrencyService currencyService;
    
    // Поля для хранения данных бюджета
    private Budget currentBudget;
    private boolean isEditMode = false;
    private List<Category> categories = new ArrayList<>();
    private List<Currency> currencies = new ArrayList<>();
    
    /**
     * Метод вызывается при создании Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_edit);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Устанавливаем заголовок
        setToolbarTitle(R.string.toolbar_title_budget_edit, R.dimen.toolbar_text_budget_edit);

        // Настройка общих кнопок редактирования
        setupCommonEditActions(R.id.save_button);

        // Инициализация всех View элементов
        budgetAmountEdit = findViewById(R.id.budget_amount_edit_text);
        budgetCategorySpinner = findViewById(R.id.budget_category_spinner);
        budgetCurrencySpinner = findViewById(R.id.budget_currency_spinner);

        // Инициализация сервисов
        budgetService = new BudgetService(this, "default_user");
        categoryService = new CategoryService(this, "default_user");
        currencyService = new CurrencyService(this, "default_user");
        
        // Настраиваем спиннеры
        setupSpinners();
        
        // Получаем данные из Intent и заполняем поля
        loadBudgetData();
    }
    
    /**
     * Настраивает спиннеры для категорий и валют
     */
    private void setupSpinners() {
        // Настройка спиннера категорий
        categoryService.getAll().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                this.categories = categories;
                
                // Создаем массив названий категорий
                String[] categoryTitles = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryTitles[i] = categories.get(i).getTitle();
                }
                
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, categoryTitles
                );
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                budgetCategorySpinner.setAdapter(categoryAdapter);
                
                Log.d(TAG, "✅ Спиннер категорий настроен: " + categories.size() + " категорий");
            }
        });
        
        // Настройка спиннера валют
        currencyService.getAll().observe(this, currencies -> {
            if (currencies != null && !currencies.isEmpty()) {
                this.currencies = currencies;
                
                // Создаем массив названий валют
                String[] currencyTitles = new String[currencies.size()];
                for (int i = 0; i < currencies.size(); i++) {
                    currencyTitles[i] = currencies.get(i).getTitle();
                }
                
                ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, currencyTitles
                );
                currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                budgetCurrencySpinner.setAdapter(currencyAdapter);
                
                Log.d(TAG, "✅ Спиннер валют настроен: " + currencies.size() + " валют");
            }
        });
    }
    
    /**
     * Загружает данные бюджета из Intent и заполняет поля
     */
    @SuppressWarnings("deprecation")
    private void loadBudgetData() {
        try {
            // Получаем бюджет из Intent
            currentBudget = (Budget) getIntent().getSerializableExtra("budget");
            
            if (currentBudget != null) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "Режим редактирования бюджета: ID=" + currentBudget.getId());
                
                // Заполняем поля данными бюджета
                budgetAmountEdit.setText(String.valueOf(currentBudget.getAmount()));
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_budget_edit, R.dimen.toolbar_text_budget_edit);
                
            } else {
                // Режим создания нового бюджета
                isEditMode = false;
                Log.d(TAG, "Режим создания нового бюджета");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_budget_add, R.dimen.toolbar_text_budget_add);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных бюджета: " + e.getMessage(), e);
            isEditMode = false;
            
            // Устанавливаем заголовок для режима создания по умолчанию
            setToolbarTitle(R.string.toolbar_title_budget_add, R.dimen.toolbar_text_budget_add);
        }
    }
    
    /**
     * Находит позицию категории в спиннере по ID
     */
    private int findCategoryPosition(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Находит позицию валюты в спиннере по ID
     */
    private int findCurrencyPosition(int currencyId) {
        for (int i = 0; i < currencies.size(); i++) {
            if (currencies.get(i).getId() == currencyId) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Выполняет валидацию и сохранение. Реализация для BaseEditActivity.
     */
    @Override
    protected boolean validateAndSave() {
        Log.d(TAG, "🔄 Сохранение бюджета...");
        
        // Получаем данные из полей
        String amountText = budgetAmountEdit.getText().toString().trim();
        int categoryPosition = budgetCategorySpinner.getSelectedItemPosition();
        int currencyPosition = budgetCurrencySpinner.getSelectedItemPosition();
        
        // Валидация
        if (TextUtils.isEmpty(amountText)) {
            showFieldError(budgetAmountEdit, "Введите сумму бюджета");
            return false;
        }
        
        if (categoryPosition == -1 || categoryPosition >= categories.size()) {
            Log.e(TAG, "❌ Не выбрана категория");
            showSpinnerError(budgetCategorySpinner, "Выберите категорию");
            return false;
        }
        
        if (currencyPosition == -1 || currencyPosition >= currencies.size()) {
            Log.e(TAG, "❌ Не выбрана валюта");
            showSpinnerError(budgetCurrencySpinner, "Выберите валюту");
            return false;
        }
        
        try {
            int amount = Integer.parseInt(amountText);
            Category selectedCategory = categories.get(categoryPosition);
            Currency selectedCurrency = currencies.get(currencyPosition);
            
            if (isEditMode) {
                // Обновляем существующий бюджет
                updateBudget(amount, selectedCategory.getId(), selectedCurrency.getId());
            } else {
                // Создаем новый бюджет
                createBudget(selectedCategory.getId(), amount, selectedCurrency.getId());
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            showFieldError(budgetAmountEdit, "Введите корректную сумму");
            Log.e(TAG, "❌ Ошибка парсинга суммы: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Создает новый бюджет
     */
    private void createBudget(int categoryId, int amount, int currencyId) {
        Log.d(TAG, "🔄 Создание бюджета: категория=" + categoryId + ", сумма=" + amount + ", валюта=" + currencyId);
        
        budgetService.create(categoryId, amount, currencyId);
        
        Log.d(TAG, "✅ Бюджет создан");
        returnToBudgets();
    }
    
    /**
     * Обновляет существующий бюджет
     */
    private void updateBudget(int amount, int categoryId, int currencyId) {
        if (currentBudget == null) {
            Log.e(TAG, "❌ currentBudget равен null, невозможно обновить бюджет");
            return;
        }
        
        Log.d(TAG, "🔄 Обновление бюджета ID: " + currentBudget.getId());
        
        currentBudget.setAmount(amount);
        currentBudget.setCategoryId(categoryId);
        currentBudget.setCurrencyId(currencyId);
        budgetService.update(currentBudget);
        
        Log.d(TAG, "✅ Бюджет обновлен");
        returnToBudgets();
    }

    /**
     * Возвращается к списку бюджетов
     */
    private void returnToBudgets() {
        Log.d(TAG, "🔄 Переходим к окну списка бюджетов");
        Intent intent = new Intent(this, BudgetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Переопределяем обработчик кнопки "Назад" для возврата к списку бюджетов
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Нажата кнопка 'Назад'");
        returnToBudgets();
    }
}
    