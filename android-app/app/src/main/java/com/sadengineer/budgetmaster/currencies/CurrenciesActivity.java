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
import com.sadengineer.budgetmaster.backend.database.DatabaseManager;
import com.sadengineer.budgetmaster.backend.entity.Currency;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CurrenciesActivity extends BaseNavigationActivity implements CurrencyAdapter.OnCurrencyClickListener {
    
    private static final String TAG = "CurrenciesActivity";
    
    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;
    private DatabaseManager databaseManager;
    
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
        
        // Инициализация DatabaseManager
        databaseManager = new DatabaseManager(this);
        Log.d(TAG, "✅ DatabaseManager создан");
        Toast.makeText(this, "DatabaseManager создан", Toast.LENGTH_SHORT).show();
        
        // Загружаем валюты из базы данных
        Log.d(TAG, "🔄 Начинаем загрузку валют...");
        Toast.makeText(this, "Начинаем загрузку валют...", Toast.LENGTH_SHORT).show();
        loadCurrenciesFromDatabase();

        // Обработчики кнопок валют
        ImageButton addCurrencyButton = findViewById(R.id.add_currency_button);
        ImageButton deleteCurrencyButton = findViewById(R.id.delete_currency_button);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем экран добавления валюты
                Intent intent = new Intent(CurrenciesActivity.this, AddCurrencyActivity.class);
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
        
        // Сначала инициализируем базу данных
        databaseManager.initializeDatabase().thenAccept(initResult -> {
            if (initResult) {
                Log.d(TAG, "✅ База данных инициализирована");
                runOnUiThread(() -> {
                    Toast.makeText(this, "База данных инициализирована", Toast.LENGTH_SHORT).show();
                });
                
                // Затем загружаем валюты
                databaseManager.executeDatabaseOperation(() -> {
                    com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase database = 
                        com.sadengineer.budgetmaster.backend.database.BudgetMasterDatabase.getDatabase(this);
                    List<Currency> currencies = database.currencyDao().getAllActiveCurrencies();
                    database.close();
                    return currencies;
                }).thenAccept(currencies -> {
                    Log.d(TAG, "✅ Загружено валют: " + (currencies != null ? currencies.size() : 0));
                    
                    runOnUiThread(() -> {
                        if (currencies != null && !currencies.isEmpty()) {
                            adapter.setCurrencies(currencies);
                            Log.d(TAG, "✅ Валюты отображены в списке");
                            Toast.makeText(this, "Загружено валют: " + currencies.size(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "⚠️ Валюты не найдены в базе данных");
                            Toast.makeText(this, "Валюты не найдены", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).exceptionally(throwable -> {
                    Log.e(TAG, "❌ Ошибка загрузки валют: " + throwable.getMessage(), throwable);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ошибка загрузки валют: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
            } else {
                Log.e(TAG, "❌ Ошибка инициализации базы данных");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_SHORT).show();
                });
            }
        }).exceptionally(throwable -> {
            Log.e(TAG, "❌ Ошибка инициализации: " + throwable.getMessage(), throwable);
            runOnUiThread(() -> {
                Toast.makeText(this, "Ошибка инициализации: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            });
            return null;
        });
    }
    
    @Override
    public void onCurrencyClick(Currency currency) {
        Log.d(TAG, "👆 Выбрана валюта: " + currency.getTitle() + " (ID: " + currency.getId() + ")");
        Toast.makeText(this, "Выбрана валюта: " + currency.getTitle(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }
} 