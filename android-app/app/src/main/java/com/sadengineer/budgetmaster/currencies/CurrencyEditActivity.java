package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import java.util.concurrent.CompletableFuture;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import com.sadengineer.budgetmaster.backend.entity.Currency;

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
        
        // Обработчики кнопок
        setupButtonHandlers();
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
                Intent intent = new Intent(this, CurrenciesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
    
    /**
     * Сохраняет валюту в базу данных
     */
    private void saveCurrency() {
        String currencyName = currencyNameEdit.getText().toString().trim();
        
        // Валидация
        if (TextUtils.isEmpty(currencyName)) {
            Log.d(TAG, "Передано пустое название валюты");
            return;
        }   

        try {
            Log.d(TAG, "🔄 Попытка сохранения валюты '" + currencyName + "'");

            // Проверяем существование валюты
            Currency existingCurrency = currencyService.getByTitle(currencyName).getValue();
            if (existingCurrency != null) {
                Log.d(TAG, "⚠️ Валюта с названием '" + currencyName + "' уже существует");
                return;
            }

            // Создаем валюту
            currencyService.create(currencyName);
            
            Log.d(TAG, "✅ Запрос на создание валюты отправлен");
            
            // Возвращаемся к списку валют
            Intent intent = new Intent(this, CurrenciesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "❌ Критическая ошибка при сохранении валюты: " + e.getMessage(), e);
        }
    }
    

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if (currencyService != null) {
        //     // CurrencyService сам управляет своим ExecutorService
        // }
    }
} 