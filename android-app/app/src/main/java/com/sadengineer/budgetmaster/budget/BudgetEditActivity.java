package com.sadengineer.budgetmaster.budget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;
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

        // Инициализация всех View элементов
        budgetAmountEdit = findViewById(R.id.budget_amount_edit_text);
        budgetCategorySpinner = findViewById(R.id.budget_category_spinner);
        budgetCurrencySpinner = findViewById(R.id.budget_currency_spinner);
        saveButton = findViewById(R.id.position_change_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

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
     * Загружает данные бюджета из Intent
     */
    private void loadBudgetData() {
        Intent intent = getIntent();
        if (intent != null) {
            int budgetId = intent.getIntExtra("budget_id", -1);
            if (budgetId != -1) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "🔄 Режим редактирования бюджета ID: " + budgetId);
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_budget_edit, R.dimen.toolbar_text);
                
                // Загружаем данные бюджета из базы
                budgetService.getById(budgetId).observe(this, budget -> {
                    if (budget != null) {
                        currentBudget = budget;
                        fillBudgetData();
                        Log.d(TAG, "✅ Данные бюджета загружены из базы");
                    } else {
                        Log.e(TAG, "❌ Бюджет с ID " + budgetId + " не найден");
                        finish();
                    }
                });
            } else {
                // Режим создания
                isEditMode = false;
                Log.d(TAG, "🔄 Режим создания нового бюджета");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_budget_add, R.dimen.toolbar_text);
            }
        }
    }
    
    /**
     * Заполняет поля данными бюджета
     */
    private void fillBudgetData() {
        if (currentBudget != null) {
            budgetAmountEdit.setText(String.valueOf(currentBudget.getAmount()));
            
            // Устанавливаем выбранную категорию
            int categoryPosition = findCategoryPosition(currentBudget.getCategoryId());
            if (categoryPosition != -1) {
                budgetCategorySpinner.setSelection(categoryPosition);
            }
            
            // Устанавливаем выбранную валюту
            int currencyPosition = findCurrencyPosition(currentBudget.getCurrencyId());
            if (currencyPosition != -1) {
                budgetCurrencySpinner.setSelection(currencyPosition);
            }
            
            Log.d(TAG, "✅ Данные бюджета загружены в поля");
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
     * Реализация абстрактного метода для валидации и сохранения
     */
    @Override
    protected boolean validateAndSave() {
        return saveBudget();
    }
    
    /**
     * Сохраняет бюджет
     */
    private boolean saveBudget() {
        Log.d(TAG, "🔄 Сохранение бюджета...");
        
        // Получаем данные из полей
        String amountText = budgetAmountEdit.getText().toString().trim();
        int categoryPosition = budgetCategorySpinner.getSelectedItemPosition();
        int currencyPosition = budgetCurrencySpinner.getSelectedItemPosition();
        
        // Валидация
        if (TextUtils.isEmpty(amountText)) {
            budgetAmountEdit.setError("Введите сумму бюджета");
            return false;
        }
        
        if (categoryPosition == -1 || categoryPosition >= categories.size()) {
            Log.e(TAG, "❌ Не выбрана категория");
            return false;
        }
        
        if (currencyPosition == -1 || currencyPosition >= currencies.size()) {
            Log.e(TAG, "❌ Не выбрана валюта");
            return false;
        }
        
        try {
            int amount = Integer.parseInt(amountText);
            Category selectedCategory = categories.get(categoryPosition);
            Currency selectedCurrency = currencies.get(currencyPosition);
            
            if (isEditMode) {
                // Обновляем существующий бюджет
                return updateBudget(amount, selectedCategory.getId(), selectedCurrency.getId());
            } else {
                // Создаем новый бюджет
                return createBudget(selectedCategory.getId(), amount, selectedCurrency.getId());
            }
            
        } catch (NumberFormatException e) {
            budgetAmountEdit.setError("Введите корректную сумму");
            Log.e(TAG, "❌ Ошибка парсинга суммы: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Создает новый бюджет
     */
    private boolean createBudget(int categoryId, int amount, int currencyId) {
        try {
            Log.d(TAG, "🔄 Создание бюджета: категория=" + categoryId + ", сумма=" + amount + ", валюта=" + currencyId);
            
            budgetService.create(categoryId, amount, currencyId);
            
            Log.d(TAG, "✅ Бюджет создан");
            returnToBudget();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка при создании бюджета: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Обновляет существующий бюджет
     */
    private boolean updateBudget(int amount, int categoryId, int currencyId) {
        if (currentBudget == null) {
            Log.e(TAG, "❌ currentBudget равен null, невозможно обновить бюджет");
            return false;
        }
        
        try {
            Log.d(TAG, "🔄 Обновление бюджета ID: " + currentBudget.getId());
            
            currentBudget.setAmount(amount);
            currentBudget.setCategoryId(categoryId);
            currentBudget.setCurrencyId(currencyId);
            budgetService.update(currentBudget);
            
            Log.d(TAG, "✅ Бюджет обновлен");
            returnToBudget();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка при обновлении бюджета: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку бюджетов
     */
    @Override
    protected void setupBackButton(int backButtonId) {
        super.setupBackButton(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
                // Возвращаемся к списку бюджетов
                returnToBudget();
            });
        }
    }
    
    /**
     * Возвращается к списку бюджетов
     */
    private void returnToBudget() {
        // Переходим к списку бюджетов
        Log.d(TAG, "🔄 Переходим к окну списка бюджетов");
        Intent intent = new Intent(this, BudgetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
