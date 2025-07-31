package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.concurrent.CompletableFuture;

// import androidx.appcompat.widget.Toolbar;

import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
// import com.sadengineer.budgetmaster.backend.entity.Currency;

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
    // private Currency editingCurrency; // null для создания новой валюты
    
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
        
        // Простой тест - показываем Toast
        android.widget.Toast.makeText(this, "CurrencyEditActivity открыта!", android.widget.Toast.LENGTH_LONG).show();
        
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
                Toast.makeText(CurrencyEditActivity.this, "Кнопка сохранения нажата!", android.widget.Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Введите название валюты", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "🔄 Сохраняем валюту: " + currencyName);
        Toast.makeText(this, "Сохраняем валюту...", Toast.LENGTH_SHORT).show();
        
        try {
            // Используем CurrencyService для создания валюты
            currencyService.create(currencyName).thenAccept(currencyId -> {
                runOnUiThread(() -> {
                    if (currencyId > 0) {
                        Toast.makeText(this, "Валюта сохранена (ID: " + currencyId + ")", Toast.LENGTH_SHORT).show();
                        
                        // Возвращаемся к списку валют
                        Intent intent = new Intent(this, CurrenciesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка: валюта не была сохранена", Toast.LENGTH_LONG).show();
                    }
                });
            }).exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Ошибка сохранения валюты: " + throwable.getMessage(), throwable);
                    Toast.makeText(this, "Ошибка сохранения: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка вызова создания валюты: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка вызова создания валюты: " + e.getMessage(), Toast.LENGTH_LONG).show();
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