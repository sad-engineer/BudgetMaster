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
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.validator.CurrencyValidator;

import java.util.concurrent.CompletableFuture;


/**
 * Activity для создания/изменения валюты
 */
public class CurrencyEditActivity extends BaseNavigationActivity {
    
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
        saveButton = findViewById(R.id.save_currency_button);
        backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_button);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");
        
        // Получаем данные из Intent и заполняем поля
        loadCurrencyData();
        
        // Обработчики кнопок
        setupButtonHandlers();
    }
    
    /**
     * Загружает данные валюты из Intent и заполняет поля
     */
    private void loadCurrencyData() {
        try {
            // Получаем валюту из Intent
            currentCurrency = (Currency) getIntent().getSerializableExtra("currency");
            
            if (currentCurrency != null) {
                // Режим редактирования
                isEditMode = true;
                Log.d(TAG, "Режим редактирования валюты: " + currentCurrency.getTitle());
                
                // Заполняем поля данными валюты
                currencyNameEdit.setText(currentCurrency.getTitle());
                
                // Устанавливаем заголовок для режима редактирования
                setToolbarTitle(R.string.toolbar_title_currency_edit, R.dimen.toolbar_text_currency_edit);
                
            } else {
                // Режим создания новой валюты
                isEditMode = false;
                Log.d(TAG, "Режим создания новой валюты");
                
                // Устанавливаем заголовок для режима создания
                setToolbarTitle(R.string.toolbar_title_currency_add, R.dimen.toolbar_text_currency_add);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки данных валюты: " + e.getMessage(), e);
            isEditMode = false;
            
            // Устанавливаем заголовок для режима создания по умолчанию
            setToolbarTitle(R.string.toolbar_title_currency_add, R.dimen.toolbar_text_currency_add);
        }
    }
    
    /**
     * Настраивает обработчики кнопок
     */
    private void setupButtonHandlers() {
        // Кнопка сохранения
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Нажата кнопка 'Сохранить'");
                saveCurrency();
            }
        });
    }
    
    /**
     * Переопределяем настройку кнопки "назад" для перехода к списку валют
     */
    @Override
    protected void setupBackButton(int backButtonId) {
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Нажата кнопка 'Назад'");
                // Возвращаемся к списку валют
                returnToCurrencies();
            });
        }
    }
    
    /**
     * Сохраняет валюту в базу данных
     */
    private void saveCurrency() {
        String currencyName = currencyNameEdit.getText().toString().trim();
        
        // Валидация названия валюты
        try {
            CurrencyValidator.validateTitle(currencyName);
        } catch (IllegalArgumentException e) {
            // при ошибке выделять поле ввода красной рамкой
            currencyNameEdit.setError("Не верное название валюты: \n" + e.getMessage());
            currencyNameEdit.requestFocus();
            return;
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
                    return;
                }

                // Если валюта не существует, то создаем её
                currencyService.create(currencyName);
                
                Log.d(TAG, "✅ Запрос на создание валюты отправлен");
            }
            
            // Возвращаемся к списку валют
            returnToCurrencies();

        } catch (Exception e) {
            Log.e(TAG, "❌ Критическая ошибка при сохранении валюты: " + e.getMessage(), e);
        }
    }
    
    /**
     * Устанавливает заголовок тулбара
     * @param titleResId - ресурс строки для заголовка
     * @param textSizeResId - ресурс размера шрифта
     */
    private void setToolbarTitle(int titleResId, int textSizeResId) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
            Log.d(TAG, "Заголовок тулбара установлен: " + getString(titleResId));
            
            // Устанавливаем размер шрифта
            float textSize = getResources().getDimension(textSizeResId);
            toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            Log.d(TAG, "Размер шрифта установлен: " + textSize + "px");
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