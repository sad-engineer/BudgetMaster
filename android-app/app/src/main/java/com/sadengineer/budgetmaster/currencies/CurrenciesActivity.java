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
import com.sadengineer.budgetmaster.backend.service.CurrencyService;
import java.util.List;

public class CurrenciesActivity extends BaseNavigationActivity implements CurrencyAdapter.OnCurrencyClickListener, CurrencyAdapter.OnSelectionChangedListener {
    
    private static final String TAG = "CurrenciesActivity";
    
    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;
    private ImageButton addCurrencyButton;
    private ImageButton deleteCurrencyButton;
    private CurrencyService currencyService;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies);

        // Инициализация навигации
        initializeNavigation();
        setupMenuButton(R.id.menu_button);
        setupBackButton(R.id.back_button);

        // Инициализация CurrencyService
        currencyService = new CurrencyService(this, "default_user");

        // Инициализация RecyclerView
        setupRecyclerView();
        
        // Загружаем валюты из базы данных
        Log.d(TAG, "🔄 Начинаем загрузку валют...");
        loadCurrenciesFromDatabase();

        // Обработчики кнопок валют
        setupButtons();
    }
    
    /**
     * Настраивает кнопки
     */
    private void setupButtons() {
        addCurrencyButton = findViewById(R.id.add_currency_button);
        deleteCurrencyButton = findViewById(R.id.delete_currency_button);

        addCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - отменяем выбор
                    cancelSelectionMode();
                } else {
                    // Запускаем окно создания валюты
                    Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
                    startActivity(intent);
                }
            }
        });

        deleteCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectionMode) {
                    // В режиме выбора - удаляем выбранные валюты
                    deleteSelectedCurrencies();
                } else {
                    // Включаем режим выбора
                    enableSelectionMode();
                }
            }
        });
    }
    
    /**
     * Включает режим выбора валют
     */
    private void enableSelectionMode() {
        isSelectionMode = true;
        
        // Меняем иконки кнопок
        addCurrencyButton.setImageResource(R.drawable.ic_back);
        deleteCurrencyButton.setImageResource(R.drawable.ic_save);
        
        // Небольшая задержка для плавного перехода
        recyclerView.postDelayed(() -> {
            adapter.setSelectionMode(true);
            adapter.setSelectionListener(this);
            Log.d(TAG, "✅ Режим выбора валют включен");
        }, 100);
    }
    
    /**
     * Отменяет режим выбора
     */
    private void cancelSelectionMode() {
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        
        // Возвращаем иконки кнопок
        addCurrencyButton.setImageResource(R.drawable.ic_add);
        deleteCurrencyButton.setImageResource(R.drawable.ic_delete);
        
        Log.d(TAG, "❌ Режим выбора валют отменен");
    }
    
    /**
     * Удаляет выбранные валюты
     */
    private void deleteSelectedCurrencies() {
        List<Currency> selectedCurrencies = adapter.getSelectedCurrencies();
        
        if (selectedCurrencies.isEmpty()) {
            Toast.makeText(this, "Выберите валюты для удаления", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "🗑️ Удаляем выбранные валюты: " + selectedCurrencies.size());
        
        // Удаляем валюты из базы данных
        for (Currency currency : selectedCurrencies) {
            try {
                currencyService.softDelete(currency);
                Log.d(TAG, "✅ Удалена валюта: " + currency.getTitle());
            } catch (Exception e) {
                Log.e(TAG, "❌ Ошибка удаления валюты " + currency.getTitle() + ": " + e.getMessage(), e);
            }
        }
        
        // Отменяем режим выбора
        cancelSelectionMode();
        
        Toast.makeText(this, "Удалено валют: " + selectedCurrencies.size(), Toast.LENGTH_SHORT).show();
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
        if (!isSelectionMode) {
            Log.d(TAG, "👆 Выбрана валюта: " + currency.getTitle() + " (ID: " + currency.getId() + ")");
            
            // Открываем экран редактирования с передачей выбранной валюты
            Intent intent = new Intent(CurrenciesActivity.this, CurrencyEditActivity.class);
            intent.putExtra("currency", currency);
            startActivity(intent);
        }
    }
    
    @Override
    public void onSelectionChanged(int selectedCount) {
        Log.d(TAG, "📊 Выбрано валют: " + selectedCount);
        
        if (selectedCount > 0) {
            Toast.makeText(this, "Выбрано валют: " + selectedCount, Toast.LENGTH_SHORT).show();
        }
    }
} 