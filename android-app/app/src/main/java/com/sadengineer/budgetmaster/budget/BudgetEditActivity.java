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
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Budget;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.validator.BudgetValidator;
import com.sadengineer.budgetmaster.formatters.CurrencyAmountFormatter;
import com.sadengineer.budgetmaster.backend.constants.ModelConstants;

import java.util.List;
import java.util.ArrayList;

/**
 * Activity для создания/изменения бюджета
 */
public class BudgetEditActivity extends BaseEditActivity<Budget> {
    
    private static final String TAG = "BudgetEditActivity";

    /** Имя пользователя по умолчанию */
    /** TODO: передлать на получение имени пользователя из SharedPreferences */
    private String userName = "default_user";
    
    // View элементы
    private EditText budgetAmountEdit;
    private Spinner budgetCategorySpinner;
    private Spinner budgetCurrencySpinner;
    private View categoryLabel; 
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;

    // Сервисы для работы с данными
    private BudgetService budgetService;
    private CurrencyService currencyService;
    private BudgetValidator validator = new BudgetValidator();
    private CurrencyAmountFormatter formatter = new CurrencyAmountFormatter();
    
    // Поля для хранения данных бюджета
    private Budget currentBudget;
    private boolean isEditMode = false;
    private List<Currency> currencies = new ArrayList<>();

    // Константы
    private static final long DEFAULT_AMOUNT = ModelConstants.DEFAULT_AMOUNT;
    
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
        categoryLabel = findViewById(R.id.category_label); // TextView с текстом "Категория"
        saveButton = findViewById(R.id.position_change_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

        // Инициализация сервисов
        budgetService = new BudgetService(this, userName);
        currencyService = new CurrencyService(this, userName);
        
        // Настраиваем спиннеры
        setupSpinners();
        
        // Получаем данные из Intent и заполняем поля
        loadBudgetData();

        // Настройка кнопки "Назад"
        setupBackButton(R.id.back_button);
    }
    
    /**
     * Настраивает спиннеры для валют (категории не загружаются, так как скрыты)
     */
    private void setupSpinners() {
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
                
                // Слушатель изменения валюты (без обновления курса)
                budgetCurrencySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        // Валюта изменена, но курс не обновляем здесь
                        Log.d(TAG, "Выбрана валюта: " + currencies.get(position).getTitle());
                    }
                    
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
                
                Log.d(TAG, "Спиннер валют настроен: " + currencies.size() + " валют");
            }
        });
    }
    

    
    /**
     * Загружает данные бюджета из Intent
     */
    @SuppressWarnings("deprecation") 
    private void loadBudgetData() {
        Intent intent = getIntent();
        if (intent != null) {
            Budget budget = (Budget) intent.getSerializableExtra("item");
            if (budget != null) {
                // Режим редактирования
                isEditMode = true;
                currentBudget = budget;
                Log.d(TAG, "Режим редактирования бюджета ID: " + budget.getId());
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_budget_edit, R.dimen.toolbar_text);
                
                // Скрываем элементы выбора категории в режиме редактирования
                hideCategorySelection();
                
                // Загружаем актуальные данные из базы
                budgetService.getById(budget.getId()).observe(this, loadedBudget -> {
                    if (loadedBudget != null) {
                        currentBudget = loadedBudget;
                        fillBudgetData();
                        Log.d(TAG, "Данные бюджета загружены из базы");
                    } else {
                        Log.e(TAG, "Бюджет с ID " + budget.getId() + " не найден");
                        finish();
                    }
                });
            } else {
                // Бюджеты создаются только через категории, поэтому всегда режим редактирования
                Log.e(TAG, "BudgetEditActivity открыт без данных бюджета. Бюджеты создаются только через категории.");
                finish();
            }
        } else {
            Log.e(TAG, "Intent равен null");
            finish();
        }
    }
    
    /**
     * Скрывает элементы выбора категории в режиме редактирования
     */
    private void hideCategorySelection() {
        if (categoryLabel != null) {
            categoryLabel.setVisibility(View.GONE);
            Log.d(TAG, "Скрыт лейбл категории");
        }
        if (budgetCategorySpinner != null) {
            budgetCategorySpinner.setVisibility(View.GONE);
            Log.d(TAG, "Скрыт спиннер категории");
        }
    }

    /**
     * Заполняет поля данными бюджета
     */
    private void fillBudgetData() {
        if (currentBudget != null) {
            Log.d(TAG, "fillBudgetData: загружаем бюджет ID=" + currentBudget.getId() + 
                      ", сумма в копейках=" + currentBudget.getAmount());
            
            // Конвертируем копейки в рубли для отображения
            double amount = currentBudget.getAmount() / 100.0;
            budgetAmountEdit.setText(formatter.format(amount));
            Log.d(TAG, "fillBudgetData: установлена сумма в рублях=" + amount);
            
            // В режиме редактирования категория не изменяется, только отображаем её ID
            Log.d(TAG, "fillBudgetData: категория бюджета ID=" + currentBudget.getCategoryId() + " (не изменяется)");
            
            // Устанавливаем выбранную валюту
            int currencyPosition = findCurrencyPosition(currentBudget.getCurrencyId());
            if (currencyPosition != -1) {
                budgetCurrencySpinner.setSelection(currencyPosition);
                Log.d(TAG, "Установлена валюта: " + currencies.get(currencyPosition).getTitle());
            }
            
            Log.d(TAG, "Данные бюджета загружены в поля");
        } else {
            Log.w(TAG, "fillBudgetData: currentBudget равен null");
        }
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
        Log.d(TAG, "Сохранение бюджета...");
        long balance = DEFAULT_AMOUNT;    
        
        // Получаем данные из полей
        String amountText = budgetAmountEdit.getText().toString().trim();
        try {
            // Конвертируем рубли в копейки
            double amount = formatter.parseSafe(amountText);
            balance = (long) (amount * 100);
            validator.validateAmount(balance);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации суммы бюджета: " + e.getMessage(), e);
            budgetAmountEdit.setError(e.getMessage());
            budgetAmountEdit.requestFocus();
            return false;
        }

        int currencyPosition = budgetCurrencySpinner.getSelectedItemPosition();
        if (currencyPosition == -1 || currencyPosition >= currencies.size()) {
            Log.e(TAG, "Не выбрана валюта");
            return false;
        }
        
        try {
            Currency selectedCurrency = currencies.get(currencyPosition);
            
            if (isEditMode) {
                // Обновляем существующий бюджет (категория не изменяется)
                return updateBudget(balance, currentBudget.getCategoryId(), selectedCurrency.getId());
            } else {
                // Создаем новый бюджет (этот код не должен выполняться)
                Log.e(TAG, "Попытка создания бюджета в режиме редактирования");
                return false;
            }
            
        } catch (NumberFormatException e) {
            budgetAmountEdit.setError("Введите корректную сумму");
            Log.e(TAG, "Ошибка парсинга суммы: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Создает новый бюджет (не используется - бюджеты создаются только через категории)
     */
    private boolean createBudget(int categoryId, long amount, int currencyId) {
        Log.e(TAG, "Попытка создания бюджета через BudgetEditActivity - недопустимо!");
        Log.e(TAG, "Бюджеты создаются только автоматически при создании категорий");
        throw new UnsupportedOperationException("Создание бюджетов через BudgetEditActivity не предусмотрено. Бюджеты создаются автоматически при создании категорий.");
    }
    
    /**
     * Обновляет существующий бюджет
     */
    private boolean updateBudget(long amount, int categoryId, int currencyId) {
        if (currentBudget == null) {
            Log.e(TAG, "currentBudget равен null, невозможно обновить бюджет");
            return false;
        }
        
        try {
            Log.d(TAG, "Обновление бюджета ID: " + currentBudget.getId() + 
                      ", новая сумма: " + amount + " копеек (" + (amount / 100.0) + " рублей)" +
                      ", категория остается: " + categoryId +
                      ", новая валюта: " + currencyId);
            
            currentBudget.setAmount(amount);
            // Категория не изменяется - оставляем как есть
            currentBudget.setCurrencyId(currencyId);
            budgetService.update(currentBudget);
            
            Log.d(TAG, "Бюджет обновлен успешно");
            returnToBudget();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обновлении бюджета: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку бюджетов
     */
    protected void setupBackButton(int backButtonId) {
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
        Log.d(TAG, "Переходим к окну списка бюджетов");
        returnTo(BudgetActivity.class, true, null);
    }
}
