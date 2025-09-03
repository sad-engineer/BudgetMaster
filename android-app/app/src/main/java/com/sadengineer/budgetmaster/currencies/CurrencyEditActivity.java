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
    private EditText currencyShortNameEdit;
    private EditText exchangeRateEdit;
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
        currencyShortNameEdit = findViewById(R.id.currency_short_name_edit);
        exchangeRateEdit = findViewById(R.id.exchange_rate_edit);
        saveButton = findViewById(R.id.position_change_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        
        // Инициализация общих действий экрана редактирования
        setupCommonEditActions(R.id.position_change_button);
        
        // Настройка кнопки "Назад"
        setupBackButton(R.id.back_button);

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");
        
        // Настраиваем поле обменного курса
        setupExchangeRateField();
        
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
                currencyShortNameEdit.setText(currentCurrency.getShortName());
                
                // Заполняем поле обменного курса
                if (currentCurrency.getExchangeRate() > 0) {
                    exchangeRateEdit.setText(String.format("%.4f", currentCurrency.getExchangeRate()));
                } else {
                    exchangeRateEdit.setText("1.0"); // По умолчанию для новой валюты
                }
                
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
    protected void setupBackButton(int backButtonId) {
        ImageButton back = findViewById(backButtonId);
        if (back != null) {
            back.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
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
     * @return true если сохранение прошло успешно, false если была ошибка валидации
     */
    private boolean saveCurrency() {
        String currencyName = currencyNameEdit.getText().toString().trim();
        String currencyShortName = currencyShortNameEdit.getText().toString().trim();
        String exchangeRateText = exchangeRateEdit.getText().toString().trim();
        
        // Валидация названия валюты
        try {
            CurrencyValidator.validateTitle(currencyName);
        } catch (IllegalArgumentException e) {
            // при ошибке выделять поле ввода красной рамкой
            Log.e(TAG, "Ошибка валидации названия валюты: " + e.getMessage(), e);
            currencyNameEdit.setError(e.getMessage());
            currencyNameEdit.requestFocus();
            return false;
        }
        
        // Валидация короткого имени валюты
        try {
            CurrencyValidator.validateShortName(currencyShortName);
        } catch (IllegalArgumentException e) {
            // при ошибке выделять поле ввода красной рамкой
            Log.e(TAG, "Ошибка валидации короткого имени валюты: " + e.getMessage(), e);
            currencyShortNameEdit.setError(e.getMessage());
            currencyShortNameEdit.requestFocus();
            return false;
        }
        
        // Валидация обменного курса
        double exchangeRate;
        try {
            if (TextUtils.isEmpty(exchangeRateText)) {
                exchangeRate = 1.0; // По умолчанию
            } else {
                exchangeRate = Double.parseDouble(exchangeRateText);
                if (exchangeRate <= 0) {
                    throw new IllegalArgumentException("Курс должен быть больше нуля");
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Ошибка валидации обменного курса: некорректный формат", e);
            exchangeRateEdit.setError("Введите корректный курс (например: 80.0)");
            exchangeRateEdit.requestFocus();
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Ошибка валидации обменного курса: " + e.getMessage(), e);
            exchangeRateEdit.setError(e.getMessage());
            exchangeRateEdit.requestFocus();
            return false;
        }

        try {
            if (isEditMode && currentCurrency != null) {
                // Режим редактирования
                Log.d(TAG, "Попытка обновления валюты '" + currencyName + "' (ID: " + currentCurrency.getId() + ")");
                
                // Обновляем данные валюты через сервис
                currentCurrency.setTitle(currencyName);
                currentCurrency.setShortName(currencyShortName);
                currentCurrency.setExchangeRate(exchangeRate);
                currencyService.update(currentCurrency);
                
                Log.d(TAG, "Запрос на обновление валюты отправлен");
                
            } else {
                // Режим создания новой валюты
                Log.d(TAG, "Попытка создания валюты '" + currencyName + "'");

                // Создаем валюту через сервис (проверки уникальности внутри сервиса)
                currencyService.create(currencyName, currencyShortName, exchangeRate);
                
                Log.d(TAG, "Запрос на создание валюты отправлен");
            }
            
            // Возвращаемся к списку валют
            returnToCurrencies();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Критическая ошибка при сохранении валюты: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Возвращается к списку валют
     */
    private void returnToCurrencies() {
        // Переходим к списку валют
        Log.d(TAG, "Переходим к окну списка валют");
        returnTo(CurrenciesActivity.class, true, new String[0]);
    }
    
    /**
     * Настраивает поле обменного курса
     */
    private void setupExchangeRateField() {
        if (exchangeRateEdit != null) {
            // Устанавливаем подсказку
            exchangeRateEdit.setHint("Обменный курс");
            
            // Добавляем валидацию для числового ввода
            exchangeRateEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            
            Log.d(TAG, "Поле обменного курса настроено");
        }
    }
} 