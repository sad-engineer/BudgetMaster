package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.base.BaseEditActivity;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;

import java.util.concurrent.CompletableFuture;


/**
 * Activity для создания/изменения валюты
 */
public class CurrencyEditActivity extends BaseEditActivity<Currency> {
    
    private static final String TAG = "CurrencyEditActivity";
    
    private EditText currencyNameEdit;
    private ImageButton saveButton;
    private ImageButton backButton;
    private ImageButton menuButton;
    private CurrencyService currencyService;
    private CurrencyValidator currencyValidator;
    
    // Поля для хранения данных валюты
    private Currency currentCurrency;
    private boolean isEditMode = false;
    
    /**
     * Метод вызывается при создании Activity
     * @param savedInstanceState - сохраненное состояние Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_edit);

        // Инициализация всех View элементов
        currencyNameEdit = findViewById(R.id.currency_name_edit);
        saveButton = findViewById(R.id.position_change_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");
        
        // Получаем данные из Intent и заполняем поля
        loadCurrencyData();
    }
    
    /**
     * Загружает данные валюты из Intent и заполняет поля
     */
    @SuppressWarnings("deprecation") 
    private void loadCurrencyData() {
        try {
            // Получаем валюту из Intent
            currentCurrency = (Currency) getIntent().getSerializableExtra("item");
            
            if (currentCurrency != null) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "Режим редактирования валюты: " + currentCurrency.getTitle());
                
                // Заполняем поля данными валюты
                currencyNameEdit.setText(currentCurrency.getTitle());
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_currency_edit, R.dimen.toolbar_text_currencies_edit);
                
            } else {
                // Режим создания новой валюты
                isEditMode = false;
                Log.d(TAG, "Режим создания новой валюты");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_currency_add, R.dimen.toolbar_text_currencies_add);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных валюты: " + e.getMessage(), e);
            isEditMode = false;
            
            // Устанавливаем заголовок для режима создания по умолчанию
            setToolbarTitle(R.string.toolbar_title_currency_add, R.dimen.toolbar_text_currencies_add);
        }
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку валют
     */
    @Override
    protected void setupBackButton(int backButtonId) {
        super.setupBackButton(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
                // Возвращаемся к списку валют
                returnToCurrencies();
            });
        }
    }
    
    /**
     * Реализация абстрактного метода для валидации и сохранения
     */
    @Override
    protected boolean validateAndSave() {
        return saveCurrency();
    }
    
    /**
     * Сохраняет валюту в базу данных
     */
    private boolean saveCurrency() {
        String currencyName = currencyNameEdit.getText().toString().trim();
        
        // Валидация названия валюты
        try {
            CurrencyValidator.validateTitle(currencyName);
        } catch (IllegalArgumentException e) {
            // при ошибке выделять поле ввода красной рамкой
            currencyNameEdit.setError("Не верное название валюты: \n" + e.getMessage());
            currencyNameEdit.requestFocus();
            return false;
        }

        try {
            if (isEditMode && currentCurrency != null) {
                // Режим редактирования
                Log.d(TAG, "🔄 Попытка обновления валюты '" + currencyName + "' (ID: " + currentCurrency.getId() + ")");
                
                // Обновляем данные валюты через сервис
                currentCurrency.setTitle(currencyName);
                currencyService.update(currentCurrency);
                
                Log.d(TAG, "✅ Запрос на обновление валюты отправлен");
                
            } else {
                // Режим создания новой валюты
                Log.d(TAG, "🔄 Попытка создания валюты '" + currencyName + "'");

                // Проверяем существование валюты
                Currency existingCurrency = currencyService.getByTitle(currencyName).getValue();
                if (existingCurrency != null) {
                    Log.d(TAG, "⚠️ Валюта с названием '" + currencyName + "' уже существует");
                    return false;
                }

                // Если валюта не существует, то создаем её
                currencyService.create(currencyName);
                
                Log.d(TAG, "✅ Запрос на создание валюты отправлен");
            }
            
            // Возвращаемся к списку валют
            returnToCurrencies();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Критическая ошибка при сохранении валюты: " + e.getMessage(), e);
            return false;
        }
    }
    


    /**
     * Возвращается к списку валют
     */
    private void returnToCurrencies() {
        // Переходим к списку валют
        Log.d(TAG, "🔄 Переходим к окну списка валют");
        Intent intent = new Intent(this, CurrenciesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }   
} 