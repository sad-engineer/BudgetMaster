package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;

public class CurrenciesActivity extends BaseNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Обработчики кнопок валют
        ImageButton addCurrencyButton = findViewById(R.id.add_currency_button);
        ImageButton deleteCurrencyButton = findViewById(R.id.delete_currency_button);

        // Обработчики кнопок валют

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать добавление валюты
                Toast.makeText(CurrenciesActivity.this, "Добавить валюту", Toast.LENGTH_SHORT).show();
            }
        });

        deleteCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Реализовать удаление валюты
                Toast.makeText(CurrenciesActivity.this, "Удалить валюту", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 