package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddCurrencyActivity extends BaseNavigationActivity {
    
    private static final String TAG = "AddCurrencyActivity";
    
    private EditText currencyNameEditText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_currency);
        
        Log.d(TAG, "🚀 AddCurrencyActivity создана");
        
        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButtonForAddCurrency(R.id.back_button);
        
        // Инициализация элементов интерфейса
        initializeViews();
        
        // Настройка обработчиков кнопок
        setupButtonHandlers();
    }

    /**
     * Инициализация элементов интерфейса
     * @param currencyNameEditText - поле ввода названия валюты
     */
    private void initializeViews() {
        currencyNameEditText = findViewById(R.id.currency_name_edit_text);
        Log.d(TAG, "✅ Элементы интерфейса инициализированы");
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран валют
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddCurrency(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "👆 Нажата кнопка назад");
                // Возвращаемся на экран валют
                Intent intent = new Intent(this, CurrenciesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }
    
    /**
     * Настройка обработчиков кнопок
     * @param saveButton - кнопка сохранения валюты
     */
    private void setupButtonHandlers() {
        // Кнопка "Сохранить"
        ImageButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "👆 Нажата кнопка сохранить");
                // TODO: Реализовать сохранение валюты
                Toast.makeText(AddCurrencyActivity.this, "Сохранить валюту (текстовая заглушка)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Запускается при запуске Activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "🚀 AddCurrencyActivity запущена");
    }
    
    /**
     * Возобновляется при возобновлении Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ AddCurrencyActivity возобновлена");
        // Возобновляем работу когда приложение снова активно
    }
    
    /**
     * Приостанавливается при паузе Activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ AddCurrencyActivity приостановлена");
        // Останавливаем обновления UI когда приложение не активно
    }
    
    /**
     * Останавливается при остановке Activity
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "🛑 AddCurrencyActivity остановлена");
    }
    
    /**
     * Уничтожается при уничтожении Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 AddCurrencyActivity уничтожена");
    }
}