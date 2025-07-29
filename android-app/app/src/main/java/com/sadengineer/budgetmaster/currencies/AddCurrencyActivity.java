package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class AddCurrencyActivity extends BaseNavigationActivity {
    
    private EditText currencyNameEditText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_currency);
        
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
     */
    private void initializeViews() {
        currencyNameEditText = findViewById(R.id.currency_name_edit_text);
    }
    
    /**
     * Настройка кнопки "назад" для возврата на экран валют
     * @param backButtonId ID кнопки назад в toolbar
     */
    private void setupBackButtonForAddCurrency(int backButtonId) {
        ImageButton backButton = findViewById(backButtonId);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
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
     */
    private void setupButtonHandlers() {
        // Кнопка "Сохранить"
        ImageButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать сохранение валюты
                Toast.makeText(AddCurrencyActivity.this, "Сохранить валюту", Toast.LENGTH_SHORT).show();
            }
        });
    }
}