package com.sadengineer.budgetmaster.currencies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sadengineer.budgetmaster.R;
import com.sadengineer.budgetmaster.navigation.BaseNavigationActivity;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase;
import java.util.List;

public class CurrenciesActivity extends BaseNavigationActivity implements CurrencyAdapter.OnCurrencyClickListener {
    
    private static final String TAG = "CurrenciesActivity";
    
    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Инициализация RecyclerView
        setupRecyclerView();
        
        // Загружаем валюты из базы данных
        Log.d(TAG, "🔄 Начинаем загрузку валют...");
        loadCurrenciesFromDatabase();

        // Обработчики кнопок валют
        ImageButton addCurrencyButton = findViewById(R.id.add_currency_button);
        ImageButton deleteCurrencyButton = findViewById(R.id.delete_currency_button);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Запускаем окно создания валюты
                Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
                startActivity(intent);
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
    
    /**
     * Настраивает RecyclerView
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.currencies_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new CurrencyAdapter(this);
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Загружает валюты из базы данных
     */
    private void loadCurrenciesFromDatabase() {
        Log.d(TAG, "🔄 Загружаем валюты из базы данных...");
        
        try {
            // Получаем базу данных (уже инициализирована в MainActivity)
            BudgetMasterDatabase database = BudgetMasterDatabase.getDatabase(this);
            
            // Загружаем валюты через Observer
            database.currencyDao().getAllActive().observe(this, currencies -> {
                Log.d(TAG, "✅ Загружено валют: " + (currencies != null ? currencies.size() : 0));
                
                if (currencies != null && !currencies.isEmpty()) {
                    adapter.setCurrencies(currencies);
                    Log.d(TAG, "✅ Валюты отображены в списке");
                } else {
                    Log.w(TAG, "⚠️ Валюты не найдены в базе данных");
                    Toast.makeText(this, "Валюты не найдены", Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Ошибка загрузки валют: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка загрузки валют: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onCurrencyClick(Currency currency) {
        Log.d(TAG, "👆 Выбрана валюта: " + currency.getTitle() + " (ID: " + currency.getId() + ")");
        Toast.makeText(this, "Выбрана валюта: " + currency.getTitle(), Toast.LENGTH_SHORT).show();
    }
} 